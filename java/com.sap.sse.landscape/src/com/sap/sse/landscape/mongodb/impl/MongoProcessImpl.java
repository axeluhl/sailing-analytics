package com.sap.sse.landscape.mongodb.impl;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.mongodb.MongoMetrics;
import com.sap.sse.landscape.mongodb.MongoProcess;

public class MongoProcessImpl implements MongoProcess {
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
    public boolean isAlive() {
        // TODO Implement MongoProcessImpl.isAlive(...)
        return false;
    }

    @Override
    public boolean isReady() {
        // TODO Implement MongoProcessImpl.isReady(...)
        return false;
    }

    @Override
    public boolean isHidden() {
        // TODO Implement MongoProcessImpl.isHidden(...)
        return false;
    }

    @Override
    public int getPriority() {
        // TODO Implement MongoProcessImpl.getPriority(...)
        return 0;
    }

    @Override
    public int getVotes() {
        // TODO Implement MongoProcessImpl.getVotes(...)
        return 0;
    }

}
