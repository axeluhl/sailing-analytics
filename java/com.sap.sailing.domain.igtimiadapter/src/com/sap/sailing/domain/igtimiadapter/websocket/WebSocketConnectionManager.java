package com.sap.sailing.domain.igtimiadapter.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ShutdownThread;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.impl.FixFactory;
import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectionFactoryImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WebSocketConnectionManager implements LiveDataConnection {
    private static final Logger logger = Logger.getLogger(WebSocketConnectionManager.class.getName());
    private final IgtimiConnectionFactoryImpl connectionFactory;
    private static enum TargetState { OPEN, CLOSED };
    private TargetState targetState;
    private WebSocketClient client;
    private final JSONObject configurationMessage;
    private final Timer timer;
    private final Iterable<String> deviceIds;
    private final Account account;
    private final FixFactory fixFactory;
    private boolean receivedServerHeartbeatInInterval;
    private final ConcurrentMap<BulkFixReceiver, BulkFixReceiver> listeners;
    private TimePoint igtimiServerTimepoint;
    private TimePoint localTimepointWhenServerTimepointWasReceived;
    
    private WebSocket currentSocket;
    
    /**
     * Counts the messages received. Every {@link #LOG_EVERY_SO_MANY_MESSAGES} an {@link Level#INFO} message is logged.
     */
    private int messageCount;
    
    private static final int LOG_EVERY_SO_MANY_MESSAGES = 100;
    
    private static final long CONNECTION_TIMEOUT_IN_MILLIS = 5000;
    
    public WebSocketConnectionManager(IgtimiConnectionFactoryImpl connectionFactory, Iterable<String> deviceSerialNumbers, Account account) throws Exception {
        this.timer = new Timer("Timer for WebSocketConnectionManager for units "+deviceSerialNumbers+" and account "+account, /* isDaemon */ true);
        this.deviceIds = deviceSerialNumbers;
        this.account = account;
        this.fixFactory = new FixFactory();
        this.connectionFactory = connectionFactory;
        this.listeners = new ConcurrentHashMap<>();
        configurationMessage = connectionFactory.getWebSocketConfigurationMessage(account, deviceSerialNumbers);
        reconnect();
        startClientHeartbeat();
        startListeningForServerHeartbeat();
    }
    
    private Session getSession() {
        return currentSocket == null ? null : currentSocket.getSession();
    }
    
    private RemoteEndpoint getRemote() {
        final Session session = getSession();
        return session == null ? null : session.getRemote();
    }
    
    /**
     * Waits until the web socket connection has been established and the initial configuration handshake has
     * successfully completed. Technically, this means that the Igtimi server timestamp has successfully been received
     * and parsed. {@link #getIgtimiServerTimePointAndWhenItWasReceived()} then holds a valid time point.
     * 
     * @param timeoutInMillis
     *            use 0 to wait indefinitely
     * @return <code>true</code> if the connection is established before the timeout occurred
     */
    @Override
    public boolean waitForConnection(long timeoutInMillis) throws InterruptedException {
        long startedToWait = System.currentTimeMillis();
        synchronized (this) {
            while (igtimiServerTimepoint == null && System.currentTimeMillis() - startedToWait < timeoutInMillis) {
                wait(timeoutInMillis);
            }
            return igtimiServerTimepoint != null;
        }
    }
    
    public void stop() throws Exception {
        logger.info("Stopping connection mananager "+this);
        targetState = TargetState.CLOSED;
        timer.cancel();
        final Session session = getSession();
        if (session != null) {
            session.disconnect();
        }
        synchronized (this) {
            if (client != null) {
                client.stop();
            }
        }
    }

    private class WebSocket extends WebSocketAdapter {
        /**
         * Reconnects once when closed and clears this flag in the process. This way, the clean-up
         * of the resources occupied by this socket and its client won't trigger yet another onWebSocketClose
         * event handling.
         */
        private boolean reconnectWhenClosed = true;
        
        @Override
        public synchronized void onWebSocketClose(int statusCode, String reason) {
            if (reconnectWhenClosed) {
                reconnectWhenClosed = false;
                final Session session = getSession();
                if (session != null) {
                    session.close();
                    try {
                        session.disconnect();
                    } catch (IOException e) {
                        logger.info("Couldn't disconnect web socket session "+session+" after having closed it");
                    }
                }
                super.onWebSocketClose(statusCode, reason);
                if (targetState == TargetState.OPEN) {
                    try {
                        reconnect();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Couldn't reconnect to Igtimi web socket in " + this, e);
                    }
                }
            }
        }
        
        @Override
        public void onWebSocketError(Throwable cause) {
            logger.log(Level.SEVERE, "Error trying to open Igtimi web socket in "+this, cause);
        }
    
        @Override
        public void onWebSocketConnect(Session session) {
            super.onWebSocketConnect(session);
            logger.info("received connection "+session+" for "+this);
            receivedServerHeartbeatInInterval = true;
            try {
                getRemote().sendString(configurationMessage.toString());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not send configuration package to Igtimi web socket server in "+this, e);
                throw new RuntimeException(e);
            }
        }
    
        @Override
        public void onWebSocketText(String message) {
            logger.finest(()->"Received "+message+" in "+this);
            if (message.equals("1")) {
                logger.fine("Received server heartbeat for "+this);
                receivedServerHeartbeatInInterval = true;
            } else if (message.startsWith("[")) {
                messageCount++;
                if (messageCount % LOG_EVERY_SO_MANY_MESSAGES == 0) {
                    logger.info("Received another "+LOG_EVERY_SO_MANY_MESSAGES+" Igtimi messages. Last message was: "+message);
                }
                List<Fix> fixes = new ArrayList<>();
                try {
                    JSONArray jsonArray = (JSONArray) new JSONParser().parse(message);
                    for (Object o : jsonArray) {
                        for (final Fix fix : fixFactory.createFixes((JSONObject) o)) {
                            fixes.add(fix);
                            if (!Util.contains(deviceIds, fix.getSensor().getDeviceSerialNumber())) {
                                logger.warning("Received fix "+fix+" in message "+message+" which is from device "+fix.getSensor().getDeviceSerialNumber()+
                                        " which this connection is not configured for: "+deviceIds);
                            }
                        }
                    }
                    logger.finest(()->"Received fixes"+fixes+" for "+this);
                    notifyListeners(fixes);
                } catch (ParseException e) {
                    logger.log(Level.SEVERE, "Error trying to parse a web socket data package coming from Igtimi "+this, e);
                }
            } else {
                // try to parse server time stamp in response to the configuration message
                synchronized (this) {
                    igtimiServerTimepoint = new MillisecondsTimePoint(Long.valueOf(message));
                    localTimepointWhenServerTimepointWasReceived = MillisecondsTimePoint.now();
                    logger.info("Received server timestamp "+igtimiServerTimepoint);
                    notifyAll();
                }
            }
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName()+", "+WebSocketConnectionManager.this.toString();
        }

        public void close() {
            reconnectWhenClosed = false;
            final Session session = getSession();
            if (session != null) {
                session.close();
            }
        }
    }
    
    public com.sap.sse.common.Util.Pair<TimePoint, TimePoint> getIgtimiServerTimePointAndWhenItWasReceived() {
        return new com.sap.sse.common.Util.Pair<TimePoint, TimePoint>(igtimiServerTimepoint, localTimepointWhenServerTimepointWasReceived);
    }

    @Override
    public void addListener(BulkFixReceiver listener) {
        listeners.put(listener, listener);
    }

    @Override
    public void removeListener(BulkFixReceiver listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(List<Fix> fixes) {
        for (BulkFixReceiver listener : listeners.keySet()) {
            try {
                listener.received(fixes);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error notifying listener "+listener+" of Igtimi fixes "+fixes, e);
            }
        }
    }

    @Override
    public String toString() {
        return "Web Socket Connection Manager for devices "+deviceIds+" and account "+account+" with web socket session "+getSession();
    }

    private void startListeningForServerHeartbeat() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!receivedServerHeartbeatInInterval) {
                        logger.info("Didn't receive server heartbeat in interval for "+WebSocketConnectionManager.this+". Reconnecting...");
                        reconnect();
                    } else {
                        receivedServerHeartbeatInInterval = false;
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error with server heartbeat in "+this, e);
                }
            }
        }, /* wait for 45s for first execution */ 45000l, /* and check again every 45s*/ 45000l);
    }

    private void startClientHeartbeat() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (getRemote() != null) {
                        logger.fine("Sending client heartbeat for " + WebSocketConnectionManager.this);
                        getRemote().sendString("1");
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Couldn't send heartbeat to Igtimi web socket session in "+WebSocketConnectionManager.this+". Will continue to try...", e);
                }
            }
        }, /* delay */ 0, /* send ping message every other minute; has to be at least every five minutes, so this should be safe */ 120000l);
    }

    private synchronized void reconnect() throws Exception {
        if (currentSocket != null) {
            currentSocket.close();
        }
        if (client != null) {
            client.stop();
            client.destroy();
        }
        IOException lastException = null;
        for (URI uri : connectionFactory.getWebsocketServers()) {
            try {
                if (uri.getScheme().equals("ws") || uri.getScheme().equals("wss")) {
                    logger.log(Level.INFO, "Trying to connect to " + uri + " for " + this);
                    client = new WebSocketClient(new SslContextFactory()) {
                        @Override
                        public void destroy() {
                            super.destroy();
                            ShutdownThread.deregister(this); // this will make sure that after destroy() no reference is being held to this client
                        }
                    };
                    client.start();
                    currentSocket = new WebSocket();
                    igtimiServerTimepoint = null;
                    client.connect(currentSocket, uri, new ClientUpgradeRequest());
                    if (waitForConnection(CONNECTION_TIMEOUT_IN_MILLIS)) {
                        logger.log(Level.INFO, "Successfully connected to " + uri + " for " + this);
                        lastException = null;
                        break; // successfully connected
                    }
                    // connection not successful; stop and destroy the client
                    client.stop();
                    client.destroy();
                    client = null;
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Couldn't connect to "+uri+" for "+this, e);
                lastException = e;
            }
        }
        targetState = TargetState.OPEN;
        if (lastException != null) {
            throw lastException;
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return getSession().getRemoteAddress();
    }
}
