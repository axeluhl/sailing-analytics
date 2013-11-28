package com.sap.sailing.domain.igtimiadapter.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class LiveDataWebSocket implements WebSocketListener {
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onWebSocketConnect(Session session) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onWebSocketText(String message) {
        // TODO Auto-generated method stub
        
    }
}
