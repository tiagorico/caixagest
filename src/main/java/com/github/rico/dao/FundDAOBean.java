package com.github.rico.dao;

import com.github.rico.entity.Fund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class FundDAOBean extends GenericDAOBean<Fund, Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundDAOBean.class);

    public Optional<Fund> findFundByUUID(UUID uuid) {
        TypedQuery<Fund> query = manager.createQuery(
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
}
