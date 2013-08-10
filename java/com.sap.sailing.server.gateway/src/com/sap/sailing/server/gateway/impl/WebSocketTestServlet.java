package com.sap.sailing.server.gateway.impl;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * A test servlet for web socket connectivity. To use from within GWT, use something like this:
 * 
 * <pre>
 *     private void testWebSockets() {
        final String baseUrl = GWT.getModuleBaseURL().replace("http", "ws");
        int indexOfGwt = baseUrl.indexOf("/gwt/");
        String url = baseUrl.substring(0, indexOfGwt)+"/sailingserver/websockettest";
        new JavaScriptWebSocketFactory().createWebSocket(url, new WebSocketCallback() {
            public void onOpen(WebSocket webSocket) {
                webSocket.send("Hello");
            }
            
            public void onMessage(WebSocket webSocket, String message) {
                ...
            }
            
            public void onError(WebSocket webSocket) {
                ...
            }
            
            public void onClose(WebSocket webSocket) {
                ...
            }
        });
    }
 * </pre>
 * @author Axel Uhl (D043530)
 *
 */
public class WebSocketTestServlet extends WebSocketServlet {
    private static final long serialVersionUID = -5160225187387804019L;

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(MyFirstWebSocket.class);
    }
    
    @WebSocket
    public static class MyFirstWebSocket extends WebSocketAdapter {
        @Override
        public void onWebSocketText(String s) {
            System.out.println(s);
            try {
                getSession().getRemote().sendString("This is my response to "+s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            super.onWebSocketBinary(payload, offset, len);
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
        }

        @Override
        public void onWebSocketConnect(Session sess) {
            super.onWebSocketConnect(sess);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            super.onWebSocketError(cause);
        }
    }
}
