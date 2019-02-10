/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.web;

import com.github.rico.business.FundsServiceBean;
import com.github.rico.model.dto.FundDto;
import com.github.rico.model.dto.RatingDto;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * TODO add a description here
 *
 * @author rico
 */
@Singleton
@Path("/funds")
public class FundsResource {

    @Inject
    private FundsServiceBean fundsServiceBean;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<FundDto> getFunds() {
        return fundsServiceBean.getFunds();
    }

    @GET
    @Path("/{id}/rates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RatingDto> getRatings(@PathParam("id") Integer id) {
        return fundsServiceBean.getRatings(id);
    }
}
