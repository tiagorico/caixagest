package com.github.rico.common;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.net.URL;

/**
 * Base class for all restful resources test classes
 * <p>
 * Created by Rico on 19/01/2017
 */
public class TestUtils {

    @ArquillianResource
    private URL webAppUrl;

    public static WebArchive getWebArchive() {
        return createCommon().addPackages(true, "com.github.rico.entity", "com.github.rico.dao");
    }

    private static WebArchive createCommon() {
        return ShrinkWrap.create(WebArchive.class, "caixagest.war")
                .addClass(ProvisionTestData.class)
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/beans.xml")
                .setWebXML("test-web.xml")
                .addAsResource("./tomee/config/test-jaas.config", "jaas.config")
                .addAsResource("./tomee/config/users.properties", "users.properties")
                .addAsResource("./tomee/config/groups.properties", "groups.properties");
    }
}
