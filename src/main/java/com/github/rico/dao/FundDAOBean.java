package com.github.rico.dao;

import com.github.rico.entity.Fund;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.UUID;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class FundDAOBean extends GenericDAOBean<Fund, UUID> {
}
