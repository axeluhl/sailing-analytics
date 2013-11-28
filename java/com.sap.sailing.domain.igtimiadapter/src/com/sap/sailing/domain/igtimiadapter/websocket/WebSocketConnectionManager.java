package com.sap.sailing.domain.igtimiadapter.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

public class WebSocketConnectionManager {
    private static final Logger logger = Logger.getLogger(WebSocketConnectionManager.class.getName());
    private final IgtimiConnectionFactory connectionFactory;
    
    public WebSocketConnectionManager(IgtimiConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void connect() throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.start();
        LiveDataWebSocket socket = new LiveDataWebSocket();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        IOException lastException = null;
        for (URI uri : connectionFactory.getWebsocketServers()) {
            try {
                logger.log(Level.INFO, "Trying to connect to "+uri);
                client.connect(socket, uri, request);
                logger.log(Level.INFO, "Successfully connected to "+uri);
                lastException = null;
                break; // successfully connected
            } catch (IOException e) {
                logger.log(Level.INFO, "Couldn't connect to "+uri, e);
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        } else {
            
        }
    }
}
