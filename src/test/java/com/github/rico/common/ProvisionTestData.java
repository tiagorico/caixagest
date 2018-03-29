package com.github.rico.common;

import com.github.rico.entity.Fund;
import com.github.rico.entity.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Roberto Cortez
 */
@Startup
@Singleton
public class ProvisionTestData {

    private static Logger LOGGER = LoggerFactory.getLogger(ProvisionTestData.class);

    @PersistenceContext(unitName = "caixagestPu")
    private EntityManager entityManager;

    @PostConstruct
    private void init() {

        LOGGER.info("************************* INSERT TEST DATA **************************************");

        //entityManager.createQuery("DELETE FROM Prediction ").executeUpdate();
        final List<Fund> funds = new ArrayList<>();
        Fund fund = Fund.builder().id(UUID.randomUUID()).name("Teste").build();
        funds.add(fund);
        funds.forEach(entityManager::persist);

        final List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.builder().date(LocalDateTime.now()).fund(fund).value(2D).build());
        ratings.forEach(entityManager::persist);

        entityManager.flush();
    }

    @PreDestroy
    private void destroy() {

        LOGGER.info("************************* DELETE TEST DATA **************************************");

        final CriteriaQuery<Object> allFunds = entityManager.getCriteriaBuilder().createQuery();
        entityManager.createQuery(allFunds.select(allFunds.from(Fund.class)))
                .getResultList()
                .forEach(entityManager::remove);


    }
}
