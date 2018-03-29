package com.github.rico.dao;

import com.github.rico.entity.ShareRating;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class ShareRatingDAOBean extends GenericDAOBean<ShareRating, Integer> {
}
