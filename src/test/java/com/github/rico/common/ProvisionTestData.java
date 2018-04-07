package com.github.rico.common;

import com.github.rico.entity.Fund;
import com.github.rico.entity.Rating;
import com.github.rico.entity.RatingID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Roberto Cortez
 */
@Startup
@Singleton
@TransactionAttribute(value = TransactionAttributeType.SUPPORTS)
public class ProvisionTestData {

    private static Logger LOGGER = LoggerFactory.getLogger(ProvisionTestData.class);

    @PersistenceContext(unitName = "caixagestPu")
    private EntityManager entityManager;

    @PostConstruct
    private void init() {

        LOGGER.info("************************* INSERT TEST DATA **************************************");

        entityManager.createNativeQuery("ALTER SEQUENCE hibernate_sequence RESTART WITH 1").executeUpdate();

        final List<Fund> funds = new ArrayList<>();
        funds.add(Fund.builder().uuid(UUID.randomUUID()).name("Teste").status(Fund.Status.ENABLE).build());
        funds.add(Fund.builder().uuid(UUID.randomUUID()).name("Teste 2").status(Fund.Status.ENABLE).build());
        funds.forEach(entityManager::persist);

        final List<Rating> ratings = new ArrayList<>();
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).stream().forEach(i ->
                ratings.add(Rating.builder().id(RatingID.builder().date(LocalDate.now().minusDays((long) i))
                        .fund(funds.get(0)).build()).value(i.doubleValue()).build())
        );
        ratings.forEach(entityManager::persist);

        entityManager.flush();
    }

    @PreDestroy
    private void destroy() {

        LOGGER.info("************************* DELETE TEST DATA **************************************");

//        final CriteriaQuery<Object> allFunds = entityManager.getCriteriaBuilder().createQuery();
//        entityManager.createQuery(allFunds.select(allFunds.from(Fund.class)))
//                .getResultList()
//                .forEach(entityManager::remove);
        entityManager.createQuery("DELETE FROM com.github.rico.entity.Rating").executeUpdate();
        entityManager.createQuery("DELETE FROM com.github.rico.entity.Fund").executeUpdate();

    }
}
