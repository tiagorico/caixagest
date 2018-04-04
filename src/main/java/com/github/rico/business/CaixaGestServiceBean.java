package com.github.rico.business;

import com.github.rico.dao.FundDAOBean;
import com.github.rico.dao.RatingDAOBean;
import com.github.rico.entity.Fund;
import com.github.rico.entity.Rating;
import com.github.rico.utils.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.rico.entity.Fund.Status.DISABLE;
import static com.github.rico.entity.Fund.Status.ENABLE;
import static com.github.rico.utils.HttpUtils.COOKIE;
import static com.github.rico.utils.HttpUtils.HTML;
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

    @Transactional(REQUIRED)
    public void updateRates(final Fund fund, final Map<String, String> values) {
        LOGGER.info("Updating funds rate.");
        final Document document = Jsoup.parse(values.get(HTML));
        final String cookie = values.get(COOKIE);

        Map<String, String> params = parseViewState(document);
        params.put(PROPERTIES.getParamFundsDropdown(), fund.getUuid().toString());

        LocalDate initialDate = ratingDAOBean.findMinDateFromFund(fund.getId()).orElse
                (LocalDate.parse(PROPERTIES.getInitialDate(), DateTimeFormatter.ofPattern(PROPERTIES
                        .getDatePattern())));
        for (; initialDate.isBefore(LocalDate.now()); initialDate = initialDate.plusDays(1)) {
            params.put(PROPERTIES.getParamDate(), initialDate.format(DateTimeFormatter.ofPattern(PROPERTIES
                    .getDatePattern())));
            params.put(PROPERTIES.getParamX(), Integer.toString(getRandom(PROPERTIES.getMaxX())));
            params.put(PROPERTIES.getParamY(), Integer.toString(getRandom(PROPERTIES.getMaxY())));

            String rate = HttpUtils.doPost(cookie, params);
            ratingDAOBean.insert(Rating.builder().date(initialDate).fund(fund).value(Double.valueOf(rate)).build());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                LOGGER.error("Thread could not sleep.", e);
            }
        }
    }

    @Transactional(REQUIRED)
    public void checkFunds() {
        LOGGER.info("Checking funds.");
        // get remote data from url
        final Map<String, String> values = HttpUtils.doGet();
        // check funds
        final List<Fund> existing = verifyFunds(Jsoup.parse(values.get(HTML)))
                .stream()
                .filter(fund -> ENABLE.equals(fund.getStatus()))
                .collect(Collectors.toList());

        LOGGER.info("Proceeding with {} funds.", existing.size());
        final ExecutorService pool = Executors.newFixedThreadPool(existing.size());
        existing.forEach(fund -> pool.execute(() -> updateRates(fund, values)));

        // shutdown workers
        shutdownAndAwaitTermination(pool);
    }

    private List<Fund> verifyFunds(Document document) {
        LOGGER.info("Checking funds.");
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

    private String parseRating(Document document) {
        return document.getElementById("cotacao").wholeText().trim();
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
                fundsList.add(Fund.builder().status(ENABLE).name(node.wholeText().trim()).uuid(UUID.fromString
                        (node.val())).build());
            }
        });
        return fundsList;
    }

    private int getRandom(int maxRandom) {
        return ThreadLocalRandom.current().nextInt(0, maxRandom + 1);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}