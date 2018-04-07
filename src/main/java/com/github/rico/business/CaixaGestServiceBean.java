package com.github.rico.business;

import com.github.rico.dao.FundDAOBean;
import com.github.rico.dao.RatingDAOBean;
import com.github.rico.entity.Fund;
import com.github.rico.entity.Rating;
import com.github.rico.entity.RatingID;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.github.rico.entity.Fund.Status.DISABLE;
import static com.github.rico.entity.Fund.Status.ENABLE;
import static com.github.rico.utils.HttpUtils.*;
import static com.github.rico.utils.SystemProperties.PROPERTIES;
import static javax.transaction.Transactional.TxType.*;

@ApplicationScoped
@Transactional(SUPPORTS)
public class CaixaGestServiceBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServiceBean.class);

    @Inject
    private FundDAOBean fundDAOBean;

    @Inject
    private RatingDAOBean ratingDAOBean;

    @Transactional(REQUIRED)
    public void updateRates(final Fund fund) {
        LOGGER.info("Updating funds rate.");
        // get remote data from url
        final Map<String, String> values = doGet();
        final Document document = Jsoup.parse(values.get(HTML));
        final String cookie = values.get(COOKIE);

        Map<String, String> params = parseViewState(document);
        params.put(PROPERTIES.getParamFundsDropdown(), fund.getUuid().toString());

        LocalDate initialDate = ratingDAOBean.findMaxDateFromFund(fund.getUuid()).orElse
                (LocalDate.parse(PROPERTIES.getInitialDate(), DateTimeFormatter.ofPattern(PROPERTIES
                        .getDatePattern())));
        initialDate = initialDate.plusDays(1);

        List<Rating> ratings = new ArrayList<>();
        for (; initialDate.isBefore(LocalDate.now()); initialDate = initialDate.plusDays(1)) {
            LOGGER.debug("Geting rates for {} on {}.", fund.getName(), initialDate.toString());
            params.put(PROPERTIES.getParamDate(), initialDate.format(DateTimeFormatter.ofPattern(PROPERTIES
                    .getDatePattern())));
            params.put(PROPERTIES.getParamX(), Integer.toString(getRandom(PROPERTIES.getMaxX())));
            params.put(PROPERTIES.getParamY(), Integer.toString(getRandom(PROPERTIES.getMaxY())));

            final Double rate = parseRating(Jsoup.parse(doPost(cookie, params)));
            LOGGER.trace("Found {}", rate);

            ratings.add(Rating.builder()
                    .value(rate)
                    .id(RatingID.builder()
                            .date(initialDate)
                            .fund(fund)
                            .build())
                    .build());
            if (ratings.size() % PROPERTIES.getBatchSize() == 0) {
                bulkInsert(ratings);
                ratings = new ArrayList<>();
            }
        }
        // finalize lasts
        bulkInsert(ratings);
    }

    @Transactional(REQUIRES_NEW)
    public void bulkInsert(List<Rating> ratings) {
        ratings.forEach(ratingDAOBean::insert);
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
        return 0.0;
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