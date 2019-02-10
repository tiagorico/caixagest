package com.github.rico.business;

import com.github.rico.dao.FundDAOBean;
import com.github.rico.dao.RatingDAOBean;
import com.github.rico.model.entity.Fund;
import com.github.rico.model.entity.Rating;
import com.github.rico.model.entity.RatingID;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.jms.Queue;
import javax.transaction.Transactional;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
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

@ApplicationScoped
@Transactional(SUPPORTS)
public class CaixaGestServiceBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServiceBean.class);

    @Inject
    private FundDAOBean fundDAOBean;

    @Inject
    private RatingDAOBean ratingDAOBean;

    @Resource(name = "JmsConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(name = "RatingsQueue")
    private Queue queue;

    @Transactional(REQUIRED)
    public void insertRating(Rating rating) {
        ratingDAOBean.insert(rating);
    }

    public void updateRates(final Fund fund) {
        // get remote data from url
        final Map<String, String> values = doGet();
        final Document document = Jsoup.parse(values.get(HTML));
        final String cookie = values.get(COOKIE);
        final Map<String, String> params = parseViewState(document);
        params.put(PROPERTIES.getParamFundsDropdown(), fund.getUuid().toString());

        LocalDate initialDate = ratingDAOBean.findMaxDateFromFund(fund.getId()).orElse
                (LocalDate.parse(PROPERTIES.getInitialDate(), DateTimeFormatter.ofPattern(PROPERTIES
                        .getDatePattern())));
        initialDate = initialDate.plusDays(1);

        LOGGER.info("Updating fund {} rates in {} days.", fund.getName(), initialDate.until(LocalDate.now(), ChronoUnit
                .DAYS));

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
                LOGGER.info("Geting rates for {} on {}.", fund.getName(), formattedDate);
                params.put(PROPERTIES.getParamDate(), formattedDate);
                params.put(PROPERTIES.getParamX(), Integer.toString(getRandom(PROPERTIES.getMaxX())));
                params.put(PROPERTIES.getParamY(), Integer.toString(getRandom(PROPERTIES.getMaxY())));

                // Make the post to caixagest and parse the result
                final Double rate = parseRating(Jsoup.parse(doPost(cookie, params)));
                if (Optional.ofNullable(rate).isPresent()) {
                    try {
                        // Create a message and send it
                        ObjectMessage message = session.createObjectMessage(
                                Rating.builder()
                                        .value(rate)
                                        .id(RatingID.builder()
                                                .date(initialDate)
                                                .fund(fund)
                                                .build())
                                        .build());
                        producer.send(message);
                    } catch (JMSException e) {
                        LOGGER.error("Error while creating jms message and send it to the queue.", e);
                    }
                } else {
                    LOGGER.info("No rates for {} on {}.", fund.getName(), formattedDate);
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
        }
    }


    @Transactional(REQUIRED)
    public List<Fund> checkFunds() {
        LOGGER.info("Checking funds.");
        // get remote data from url
        final Map<String, String> values = doGet();
        return verifyFunds(Jsoup.parse(values.get(HTML)))
                .stream()
                .filter(fund -> ENABLE.equals(fund.getStatus()))
                .collect(Collectors.toList());
    }

    private List<Fund> verifyFunds(Document document) {
        LOGGER.debug("Verifying funds.");
        final List<Fund> existing = fundDAOBean.findAll();
        final List<Fund> newOnes = parseFunds(document);

        newOnes.forEach(fund -> {
            if (!existing.contains(fund)) {
                LOGGER.info("Found new {}.", fund);
                existing.add(fundDAOBean.insert(fund));
            }
        });
        existing.forEach(oldFund -> {
            if (!newOnes.contains(oldFund)) {
                LOGGER.info("Disabling old {}.", oldFund);
                oldFund.setStatus(DISABLE);
            }
        });
        return existing;
    }

    private Double parseRating(Document document) {
        Optional<Element> node = Optional.ofNullable(document.getElementById("cotacao"));
        if (node.isPresent()) {
            NumberFormat format = NumberFormat.getInstance();
            try {
                Number number = format.parse(trim(node.get().wholeText()).replace(",", "."));
                return number.doubleValue();
            } catch (ParseException e) {
                LOGGER.error("Could not parse rating: {}", node.get().wholeText());
            }
        }
        return null;
    }

    private Map<String, String> parseViewState(Document doc) {
        Map<String, String> params = new HashMap<>(2);
        params.put(PROPERTIES.getParamViewstate(), doc.select("input[name=" + PROPERTIES.getParamViewstate() + "]").val());
        params.put(PROPERTIES.getParamViewStateGenerator(), doc.select("input[name=" + PROPERTIES
                .getParamViewStateGenerator() + "]").val());
        return params;

    }

    private List<Fund> parseFunds(Document document) {
        List<Fund> fundsList = new ArrayList<>();
        document.getElementsByTag("select").get(1).select("option[value!=\"\"]").forEach(node -> {
            if (node.val().compareTo(node.wholeText()) != 0) {
                fundsList.add(Fund.builder().status(ENABLE).name(trim(node.wholeText())).uuid(UUID.fromString
                        (node.val())).build());
            }
        });
        return fundsList;
    }

    private int getRandom(int maxRandom) {
        return ThreadLocalRandom.current().nextInt(0, maxRandom + 1);
    }

    private String trim(final String toBeTrimmed) {
        return toBeTrimmed.replace("\u00A0", "").trim();
    }

}