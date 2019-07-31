/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.web;

import com.github.rico.business.FundServiceBean;
import com.github.rico.model.dto.FundDto;
import com.github.rico.model.dto.RateDto;
import com.github.rico.model.dto.StatisticDTO;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Restfull webservice for Fund resource.
 * Provide methods to retrieve information regarding funds
 *
 * @author rico
 */
@Singleton
@Path("/funds")
public class FundsResource {

    @Inject
    private FundServiceBean fundServiceBean;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<FundDto> getFunds() {
        return fundServiceBean.getFunds();
    }

    @GET
    @Path("/{fundId}/rates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RateDto> getRates(@PathParam("fundId") Integer fundId) {
        return fundServiceBean.getRates(fundId);
    }

    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticDTO> getStatistics() {
        return fundServiceBean.getStatistics();
    }
}
