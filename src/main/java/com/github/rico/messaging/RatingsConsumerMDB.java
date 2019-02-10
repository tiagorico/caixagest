/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.messaging;

import com.github.rico.business.CaixaGestServiceBean;
import com.github.rico.model.entity.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.REQUIRED;

@MessageDriven(name = "RatingsConsumerMDB", activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "RatingsQueue")})
@Transactional(REQUIRED)
public class RatingsConsumerMDB implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingsConsumerMDB.class);

    @Inject
    private CaixaGestServiceBean service;

    public void onMessage(Message message) {
        try {
            Rating rate = (Rating) ((ObjectMessage) message).getObject();
            LOGGER.debug("Received message.", rate.toString());
            service.insertRating(rate);
        } catch (JMSException e) {
            LOGGER.error("Error while on received message.", e);
        }
    }
}
