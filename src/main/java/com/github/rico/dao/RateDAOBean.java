package com.github.rico.dao;

import com.github.rico.model.entity.Rate;
import com.github.rico.model.entity.RateID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static javax.transaction.Transactional.TxType.SUPPORTS;

/**
 * This class is responsible for all database access for the Rate entity.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@ApplicationScoped
@Transactional(SUPPORTS)
public class RateDAOBean extends GenericDAOBean<Rate, RateID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateDAOBean.class);

    /**
     * Finds all Rates from a certain Fund.
     *
     * @param fundId the fund identifier
     * @return a list of Rates
     */
    public List<Rate> findAllFromFund(final Integer fundId) {
        final TypedQuery<Rate> query = manager.createQuery(
                "SELECT r FROM Rate r WHERE r.id.fund.id = :fundId ORDER BY r.id.date ASC", Rate.class);
        return query.setParameter("fundId", fundId).getResultList();
    }

    public Optional<LocalDate> findMaxDateFromFund(final Integer fundId) {
        final TypedQuery<LocalDate> query = manager.createQuery(
                "SELECT max(r.id.date) FROM Rate r WHERE r.id.fund.id = :fundId", LocalDate.class);
        query.setParameter("fundId", fundId);

        Optional<LocalDate> result = empty();
        try {
            result = ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            LOGGER.debug("No result found.", e);
        }
        return result;
    }

    public List<Object> findMetrics(final Integer fundId) {
        final TypedQuery<Object> query = manager.createQuery(
                "SELECT max(r.value), r.id.date, min(r.value), r.id.date " +
                        "FROM Rate r WHERE r.id.fund.id = :fundId",
                Object.class);
        query.setParameter("fundId", fundId);

        return query.getResultList();
    }
}
