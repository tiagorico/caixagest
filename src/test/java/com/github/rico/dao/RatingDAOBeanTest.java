package com.github.rico.dao;

import com.github.rico.common.BaseResourceTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * TODO add a description here
 *
 * @author rico
 */
@RunWith(Arquillian.class)
public class RatingDAOBeanTest extends BaseResourceTest {

    @Inject
    private RatingDAOBean ratingDAOBean;

    @Deployment
    public static WebArchive createDeployment() {
        return getWebArchive();
    }

    @Test
    public void testFindAll() {
        assertTrue(ratingDAOBean.findAll().size() > 0);
    }

}
