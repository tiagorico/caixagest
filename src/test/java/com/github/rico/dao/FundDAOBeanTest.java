package com.github.rico.dao;

import com.github.rico.common.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static junit.framework.TestCase.assertTrue;

/**
 * TODO add a description here
 *
 * @author rico
 */
@RunWith(Arquillian.class)
public class FundDAOBeanTest extends TestUtils {

    @Inject
    private FundDAOBean fundDAOBean;

    @Deployment
    public static WebArchive createDeployment() {
        return getWebArchive();
    }

    @Test
    public void testFindAll() {
        System.out.println(fundDAOBean.findAll().size());
        assertTrue(fundDAOBean.findAll().size() > 0);
    }

}
