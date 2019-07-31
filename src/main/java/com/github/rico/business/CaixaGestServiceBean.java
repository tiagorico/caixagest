package com.github.rico.business;

import com.github.rico.dao.FundDAOBean;
import com.github.rico.dao.RateDAOBean;
import com.github.rico.model.entity.Fund;
import com.github.rico.model.entity.Rate;
import com.github.rico.model.entity.RateID;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.*;
import javax.transaction.Transactional;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.github.rico.model.entity.Fund.Status.DISABLE;
import static com.github.rico.model.entity.Fund.Status.ENABLE;
import static com.github.rico.utils.HttpUtils.*;
import static com.github.rico.utils.SystemProperties.PROPERTIES;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;

/**
 * This main purpose of this class is to dispose methods to deal with caixagest website.
 * It contains business logic to retrieve funds and rates and insert them into database.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@ApplicationScoped
@Transactional(SUPPORTS)
public class CaixaGestServiceBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServiceBean.class);

    @Inject
    private FundDAOBean fundDAOBean;

    @Inject
    private RateDAOBean rateDAOBean;

    @Resource(name = "JmsConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(name = "RatesQueue")
    private Queue queue;

    /**
     * Inserts a rate record in the database.
     * @param rate the rate entity to be inserted.
     */
    @Transactional(REQUIRED)
    public void insertRate(final Rate rate) {
        rateDAOBean.insert(rate);
    }

    /**
     * Fetch rates data from Caixagest from a certain fund.
     *
     * @param fund the fund to be used querying caixagest for rates.
     */
    public void fetchRates(final Fund fund) {
        final LocalDateTime startTime = LocalDateTime.now();
        LOGGER.info("Starting at {} to fetch rates for fund \"{}\".", startTime, fund.getName());

        // get remote data from url
        final Map<String, String> values = doGet();
        final Document document = Jsoup.parse(values.get(HTML));
        final String cookie = values.get(COOKIE);
        final Map<String, String> params = parseViewState(document);
        params.put(PROPERTIES.getParamFundsDropdown(), fund.getUuid().toString());

        LocalDate initialDate = rateDAOBean.findMaxDateFromFund(fund.getId()).orElse
                (LocalDate.parse(PROPERTIES.getInitialDate(), DateTimeFormatter.ofPattern(PROPERTIES
                        .getDatePattern())));
        initialDate = initialDate.plusDays(1);

        long daysToUpdate = initialDate.until(LocalDate.now(), ChronoUnit.DAYS);
        if (daysToUpdate == 0) {
            LOGGER.info("No need to fetch rates for fund \"{}\", all data is updated.", fund.getName());
            return;
        }

        LOGGER.info("Updating fund \"{}\" rates for the last {} days.", fund.getName(), daysToUpdate);

        Connection connection = null;
        Session session = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // For each day makes the call to get results
            for (; initialDate.isBefore(LocalDate.now()); initialDate = initialDate.plusDays(1)) {
                final String formattedDate = initialDate.format(DateTimeFormatter.ofPattern(PROPERTIES.getDatePattern()));
                LOGGER.debug("Geting rates for \"{}\" on day {}.", fund.getName(), formattedDate);
                params.put(PROPERTIES.getParamDate(), formattedDate);
                params.put(PROPERTIES.getParamX(), Integer.toString(generateRandom(PROPERTIES.getMaxX())));
                params.put(PROPERTIES.getParamY(), Integer.toString(generateRandom(PROPERTIES.getMaxY())));

                // Make the post to caixagest and parse the result
                final Double rate = parseRate(Jsoup.parse(doPost(cookie, params)));
                if (Optional.ofNullable(rate).isPresent()) {
                    try {
                        // Create a message and send it
                        ObjectMessage message = session.createObjectMessage(
                                Rate.builder()
                                        .value(rate)
                                        .id(RateID.builder().date(initialDate).fund(fund).build())
                                        .build());
                        producer.send(message);
                    } catch (JMSException e) {
                        LOGGER.error("Error while creating jms message and send it to the queue.", e);
                    }
                } else {
                    LOGGER.debug("No rates for \"{}\" on day {}.", fund.getName(), formattedDate);
                }
            }
        } catch (JMSException e) {
            LOGGER.error("Error creating connection and message producer.", e);
        } finally {
            // Clean up
            try {
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                LOGGER.error("Error closing session.", e);
            }
            LOGGER.info("Ending from fetching rates for fund \"{}\". Took me {} seconds to complete.", fund.getName(), startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
        }
    }

    /**
     * Fetch all funds from Caixagest and compares them with existing ones.
     * Only returns funds that are enabled by comparison between database and fetch data.
     *
     * @return a list of funds.
     */
    @Transactional(REQUIRED)
    public List<Fund> fetchFunds() {
        final LocalDateTime startTime = LocalDateTime.now();
        LOGGER.info("Starting at {} to fetch funds.", startTime);
        // get remote data from url
        final Map<String, String> values = doGet();
        // return only valid ones
        final List<Fund> funds = verifyFunds(Jsoup.parse(values.get(HTML)))
                .stream()
                .filter(fund -> ENABLE.equals(fund.getStatus()))
                .collect(Collectors.toList());
        LOGGER.info("Ending from fetching funds. Took me {} seconds to complete.", startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
        return funds;
    }

    /**
     * Verifies existing database funds with the ones fetched from caixagest.
     * If a fund does not exist in caixagest any longer it will be disabled in database.
     *
     * @param document the dom document to be parsed.
     * @return a list of funds.
     */
    private List<Fund> verifyFunds(final Document document) {
        LOGGER.debug("Verifying funds.");
        final List<Fund> existing = fundDAOBean.findAll();
        final List<Fund> newOnes = parseFund(document);

        newOnes.forEach(fund -> {
            if (!existing.contains(fund)) {
                LOGGER.debug("Found new fund \"{}\".", fund.getName());
                existing.add(fundDAOBean.insert(fund));
            }
        });
        existing.forEach(oldFund -> {
            if (!newOnes.contains(oldFund)) {
                LOGGER.debug("Disabling old fund \"{}\".", oldFund.getName());
                oldFund.setStatus(DISABLE);
            }
        });
        return existing;
    }

    /**
     * Parses rates from a DOM document.
     *
     * @param document the document to read from
     * @return a double value representing the rating value.
     */
    private Double parseRate(final Document document) {
        Optional<Element> node = Optional.ofNullable(document.getElementById("cotacao"));
        if (node.isPresent()) {
            try {
                final Number number = NumberFormat.getInstance().parse(replaceWhiteSpaces(node.get().wholeText()).replace(",", "."));
                return number.doubleValue();
            } catch (ParseException e) {
                LOGGER.error("Could not parse rating: {}", node.get().wholeText());
            }
        }
        return null;
    }

    /**
     * Parses VIEW_STATE properties from a DOM document.
     *
     * @param document the document to read from.
     * @return a hashmap containing the view state properties.
     */
    private Map<String, String> parseViewState(final Document document) {
        final Map<String, String> params = new HashMap<>(2);
        params.put(PROPERTIES.getParamViewstate(), document.select("input[name=" + PROPERTIES.getParamViewstate() + "]").val());
        params.put(PROPERTIES.getParamViewStateGenerator(), document.select("input[name=" + PROPERTIES
                .getParamViewStateGenerator() + "]").val());
        return params;
    }

    /**
     * Parses a fund from a DOM document.
     *
     * @param document the document to read from
     * @return a list of funds obtained.
     */
    private List<Fund> parseFund(final Document document) {
        final List<Fund> fundsList = new ArrayList<>();
        document.getElementsByTag("select").get(1).select("option[value!=\"\"]").forEach(node -> {
            if (node.val().compareTo(node.wholeText()) != 0) {
                fundsList.add(Fund.builder().status(ENABLE).name(replaceWhiteSpaces(node.wholeText())).uuid(UUID.fromString
                        (node.val())).build());
            }
        });
        return fundsList;
    }

    /**
     * Generates a random number.
     *
     * @param maxRandom the max random number
     * @return an int value
     */
    private int generateRandom(final int maxRandom) {
        return ThreadLocalRandom.current().nextInt(0, maxRandom + 1);
    }

    /**
     * Removes the encoded white spaces from a string and trims it.
     *
     * @param original the original string.
     * @return a new string with white spaces replaced and trimmed.
     */
    private String replaceWhiteSpaces(final String original) {
        return original.replace("\u00A0", "").trim();
    }

}
