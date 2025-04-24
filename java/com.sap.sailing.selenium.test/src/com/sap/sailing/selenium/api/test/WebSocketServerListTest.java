package com.sap.sailing.selenium.api.test;

import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;

public class WebSocketServerListTest extends AbstractSeleniumTest {
    @Rule public Timeout AbstractTracTracLiveTestTimeout = Timeout.millis(2 * 60 * 1000);

    @Test
    public void testWebSocketServerList() throws Exception {
        final IgtimiConnectionFactory igtimiConnectionFactory = IgtimiConnectionFactory.create(
                new URL(getContextRoot()), /* defaultBearerToken */ null); // websocket servers list also for anonymous user
        final Iterable<URI> serverUris = igtimiConnectionFactory.getOrCreateConnection().getWebsocketServers();
        assertFalse(Util.isEmpty(serverUris));
        for (final URI uri : serverUris) {
            uri.getScheme().startsWith("ws");
        }
    }
}
