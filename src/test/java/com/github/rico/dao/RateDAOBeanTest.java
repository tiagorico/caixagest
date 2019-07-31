package com.github.rico.dao;

import com.github.rico.common.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * TODO add a description here
 *
 * @author rico
 */
@RunWith(Arquillian.class)
public class RateDAOBeanTest extends TestUtils {

    @Inject
    private RateDAOBean rateDAOBean;

    @Deployment
    public static WebArchive createDeployment() {
        return getWebArchive();
    }

    @Test
    public void testFindAll() {
        assertTrue(rateDAOBean.findAll().size() > 0);
    }

    @Test
    public void testLastDate() {
        assertTrue(rateDAOBean.findMaxDateFromFund(1).isPresent());
        assertTrue(!rateDAOBean.findMaxDateFromFund(2).isPresent());
    }

}
