/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.business;

import com.github.rico.dao.FundDAOBean;
import com.github.rico.dao.RatingDAOBean;
import com.github.rico.model.dto.FundDto;
import com.github.rico.model.dto.RatingDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO add a description here
 *
 * @author rico
 */
@ApplicationScoped
public class FundsServiceBean {

    @Inject
    private FundDAOBean fundDAOBean;

    @Inject
    private RatingDAOBean ratingDAOBean;

    public List<FundDto> getFunds() {
        return fundDAOBean.findAll().stream().map(f -> FundDto.builder().id(f.getId()).name(f.getName()).build())
                .collect(Collectors.toList());
    }

    public List<RatingDto> getRatings(Integer id) {
        return ratingDAOBean.findAllFromFund(id)
                .stream()
                .map(r -> RatingDto.builder()
                        .date(r.getId().getDate().format(DateTimeFormatter.ISO_DATE))
                        .value(r.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
