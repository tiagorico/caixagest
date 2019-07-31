package com.github.rico.dao;

import com.github.rico.model.entity.Fund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.transaction.Transactional.TxType.SUPPORTS;

/**
 * This class is responsible for all database access for the Fund entity.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@ApplicationScoped
@Transactional(SUPPORTS)
public class FundDAOBean extends GenericDAOBean<Fund, Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundDAOBean.class);

    /**
     * Finds all Funds from database.
     *
     * @return a list of Fund entities
     */
    @Override
    public List<Fund> findAll() {
        return manager.createQuery("FROM Fund ORDER BY id").getResultList();
    }

    /**
     * Finds a Fund entity from it's unique UUID.
     *
     * @param uuid the uuid
     * @return a optional object containing or not the Fund
     */
    public Optional<Fund> findFundByUUID(final UUID uuid) {
        final TypedQuery<Fund> query = manager.createQuery(
                "SELECT f FROM Fund f WHERE f.uuid = :uuid", Fund.class);
        query.setParameter("uuid", uuid);
        Optional<Fund> result = empty();
        try {
            result = of(query.getSingleResult());
        } catch (NoResultException e) {
            LOGGER.debug("No result found.", e);
        }
        return result;
    }

    /**
     * Finds all Funds statistics from database.
     *
     * @return a list of Fund entities.
     */
    public List<Fund> findStatistics() {
        return (List<Fund>) manager.createNamedQuery("Funds.Statistics").getResultList();
    }
}
