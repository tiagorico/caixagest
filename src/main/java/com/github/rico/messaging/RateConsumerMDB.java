/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.messaging;

import com.github.rico.business.CaixaGestServiceBean;
import com.github.rico.model.entity.Rate;
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

/**
 * Message driven bean responsible for processing all messages from the Rates Queue.
 *
 * @author Luis Rico
 * @since 1.0.0
 */
@MessageDriven(name = "RateConsumerMDB", activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "RatesQueue")})
@Transactional(REQUIRED)
public class RateConsumerMDB implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateConsumerMDB.class);

    @Inject
    private CaixaGestServiceBean service;

    /**
     * The method responsible for processing the message.
     *
     * @param message the object message
     */
    public void onMessage(Message message) {
        try {
            final Rate rate = (Rate) ((ObjectMessage) message).getObject();
            LOGGER.debug("Received message {}.", rate.toString());
            service.insertRate(rate);
        } catch (JMSException e) {
            LOGGER.error("Error while on received message.", e);
        }
    }
}
