package com.github.rico.dao;

import com.github.rico.entity.Rating;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class RatingDAOBean extends GenericDAOBean<Rating, Integer> {
}
