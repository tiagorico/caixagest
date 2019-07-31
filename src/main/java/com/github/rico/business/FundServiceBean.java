/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.business;

import com.github.rico.dao.FundDAOBean;
import com.github.rico.dao.RateDAOBean;
import com.github.rico.model.dto.FundDto;
import com.github.rico.model.dto.RateDto;
import com.github.rico.model.dto.StatisticDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.rico.utils.SystemProperties.PROPERTIES;

/**
 * Business logic for the Fund entities.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@ApplicationScoped
public class FundServiceBean {

    @Inject
    private FundDAOBean fundDAOBean;

    @Inject
    private RateDAOBean rateDAOBean;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(PROPERTIES.getDecimalPattern());

    /**
     * Get all funds from database.
     *
     * @return a list of funds
     */
    public List<FundDto> getFunds() {
        return fundDAOBean.findAll().stream().map(f -> FundDto.builder().id(f.getId()).name(f.getName()).build())
                .collect(Collectors.toList());
    }

    /**
     * Get all Rates from a specified fund.
     *
     * @param fundId the fund identifier
     * @return a list of rates
     */
    public List<RateDto> getRates(final Integer fundId) {
        return rateDAOBean.findAllFromFund(fundId)
                .stream()
                .map(r -> RateDto.builder()
                        .date(r.getId().getDate().format(DateTimeFormatter.ISO_DATE))
                        .value(r.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get statistics for all funds.
     *
     * @return a list of statistics
     */
    public List<StatisticDTO> getStatistics() {
        return fundDAOBean.findStatistics()
                .stream()
                .map(f -> StatisticDTO.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .average(Double.valueOf(DECIMAL_FORMAT.format(f.getAverage())))
                        .standardDeviation(Double.valueOf(DECIMAL_FORMAT.format(f.getStandardDeviation())))
                        .max(Double.valueOf(DECIMAL_FORMAT.format(f.getMax())))
                        .min(Double.valueOf(DECIMAL_FORMAT.format(f.getMin())))
                        .today(Double.valueOf(DECIMAL_FORMAT.format(f.getToday())))
                        .yesterday(Double.valueOf(DECIMAL_FORMAT.format(f.getYesterday())))
                        .dayBeforeYesterday(Double.valueOf(DECIMAL_FORMAT.format(f.getDayBeforeYesterday())))
                        .build())
                .collect(Collectors.toList());
    }
}
