package com.sap.sse.landscape.mongodb.impl;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.mongodb.MongoMetrics;
import com.sap.sse.landscape.mongodb.MongoProcess;

public class MongoProcessImpl extends MongoEndpointImpl implements MongoProcess {
    private static final String LOCAL_DB_NAME = "local";
    private static final Logger logger = Logger.getLogger(MongoProcessImpl.class.getName());
    private final int port;
    private final Host host;
    
    public MongoProcessImpl(Host host) {
        this(DEFAULT_PORT, host);
    }

    public MongoProcessImpl(int port, Host host) {
        super();
        this.port = port;
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Host getHost() {
        return host;
    }

    @Override
    public RotatingFileBasedLog getLog() {
        // TODO Implement MongoProcessImpl.getLog(...)
        return null;
    }

    @Override
    public MongoMetrics getMetrics() {
        // TODO Implement MongoProcessImpl.getMetrics(...)
        return null;
    }

    @Override
    public boolean isReady() {
        try {
            return Util.contains(getClient().listDatabaseNames(), LOCAL_DB_NAME);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Internal error constructing MongoDB client URI", e);
            throw new RuntimeException(e);
        }
    }

}
