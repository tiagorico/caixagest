package com.github.rico.dao;

import com.github.rico.entity.Rating;
import com.github.rico.entity.RatingID;
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

@ApplicationScoped
@Transactional(SUPPORTS)
public class RatingDAOBean extends GenericDAOBean<Rating, RatingID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingDAOBean.class);

    public List<Rating> findAllFromFund(Integer fundId) {
        TypedQuery<Rating> query = manager.createQuery(
                "SELECT r FROM Rating r WHERE r.id.fund.id = :fundId ORDER BY r.id.date ASC", Rating.class);
        return query.setParameter("fundId", fundId).getResultList();
    }

    public Optional<LocalDate> findMaxDateFromFund(Integer fundId) {
        TypedQuery<LocalDate> query = manager.createQuery(
                "SELECT max(r.id.date) FROM Rating r WHERE r.id.fund.id = :fundId", LocalDate.class);
        query.setParameter("fundId", fundId);

        Optional<LocalDate> result = empty();
        try {
            result = ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            LOGGER.debug("No result found.", e);
        }
        return result;
    }

}
