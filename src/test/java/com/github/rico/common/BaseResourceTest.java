/*
 * Copyright (c) Stratio Lda., All Rights Reserved.
 * (www.stratio.pt)
 *
 * This software is the proprietary information of Stratio Lda.
 * Use is subject to license terms.
 */

package com.github.rico.common;

import com.stratio.jam.api.web.AuthHeaderRequestFilter;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Base class for all restful resources test classes
 * <p>
 * Created by Rico on 19/01/2017
 */
public class BaseResourceTest {

    @ArquillianResource
    private URL webAppUrl;

    public static WebArchive getWebArchive() {
        return createCommon().addPackages(true,
                "com.stratio.jam.api.business",
                "com.stratio.jam.api.entity",
                "com.stratio.jam.api.exception",
                "com.stratio.jam.api.model",
                "com.stratio.jam.api.persistence",
                "com.stratio.jam.api.rest",
                "com.stratio.jam.api.utils",
                "com.stratio.jam.api.web",
                "com.stratio.jam.api.websocket");
    }

    public static WebArchive getWebArchiveWithRedis() {
        return createCommon().addPackages(true, "com.stratio.jam.api");
    }

    public Session connectToServer(Class<?> endpoint, String uriPart) throws DeploymentException, IOException,
            URISyntaxException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI("ws://"
                + webAppUrl.getHost()
                + ":"
                + webAppUrl.getPort()
                + webAppUrl.getPath()
                + uriPart);
        return container.connectToServer(endpoint, uri);
    }

    public WebTarget getWebTarget(String path) {
        return ClientBuilder.newClient().register(AuthHeaderRequestFilter.class).target(webAppUrl.toExternalForm())
                .path(path);
    }

    private static WebArchive createCommon() {
        return ShrinkWrap.create(WebArchive.class, "jam.war")
                .addClass(ProvisionTestData.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsResource("META-INF/beans.xml")
                .setWebXML("test-web.xml")
                .addAsResource("tomee/config/test-jaas.config", "jaas.config")
                .addAsResource("tomee/config/users.properties", "users.properties")
                .addAsResource("tomee/config/groups.properties", "groups.properties");
    }
}
