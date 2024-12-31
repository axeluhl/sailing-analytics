package com.sap.sailing.domain.igtimiadapter.impl;

import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sse.common.Util;

public class WebSocketServerListTest {
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    @Test
    public void testWebSocketServerList() throws Exception {
        final IgtimiConnectionFactoryImpl igtimiConnectionFactory = new IgtimiConnectionFactoryImpl(
                new URL("http://127.0.0.1:8888"), /* defaultBearerToken */ null); // websocket servers list also for anonymous user
        final Iterable<URI> serverUris = igtimiConnectionFactory.getOrCreateConnection().getWebsocketServers();
        assertFalse(Util.isEmpty(serverUris));
        for (final URI uri : serverUris) {
            uri.getScheme().startsWith("ws");
        }
    }
}
