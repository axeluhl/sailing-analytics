package com.sap.sailing.domain.igtimiadapter.impl;

import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.igtimiadapter.persistence.PersistenceFactory;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public class WebSocketServerListTest {
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    @Test
    public void testWebSocketServerList() throws Exception {
        final MongoDBConfiguration mongoTestConfig = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService mongoTestService = mongoTestConfig.getService();
        RiotServer riotServer = RiotServer.create(PersistenceFactory.INSTANCE.getDomainObjectFactory(mongoTestService),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(mongoTestService));
        final IgtimiConnectionFactoryImpl igtimiConnectionFactory = new IgtimiConnectionFactoryImpl(
                new URL("http://127.0.0.1:8888"), /* defaultBearerToken */ null); // TODO bug6059: connect to the RiotServer launched above
        final Iterable<URI> serverUris = igtimiConnectionFactory.createConnection().getWebsocketServers();
        assertFalse(Util.isEmpty(serverUris));
        for (final URI uri : serverUris) {
            uri.getScheme().startsWith("ws");
        }
    }
}
