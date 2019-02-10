/*
 * Copyright (c) Present Technologies Lda., All Rights Reserved.
 * (www.present-technologies.com)
 *
 * This software is the proprietary information of Present Technologies Lda.
 * Use is subject to license terms.
 */
package com.github.rico.business;

import com.github.rico.common.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * TODO add a description here
 *
 * @author rico
 */
@RunWith(Arquillian.class)
public class CaixaGestServiceBeanTest extends TestUtils {

    @Inject
    private CaixaGestServiceBean caixaGestServiceBean;

    @Deployment
    public static WebArchive createDeployment() {
        return getWebArchive();
    }


}
