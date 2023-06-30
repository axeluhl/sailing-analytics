package com.sap.sse.landscape.mongodb.impl;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.common.shared.MongoDBConstants;
import com.sap.sse.landscape.mongodb.MongoMetrics;
import com.sap.sse.landscape.mongodb.MongoProcess;

/**
 * Equality and hash code are based on the host and port.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MongoProcessImpl extends MongoEndpointImpl implements MongoProcess {
    private static final String LOCAL_DB_NAME = "local";
    private static final Logger logger = Logger.getLogger(MongoProcessImpl.class.getName());
    private final int port;
    private final Host host;
    
    public MongoProcessImpl(Host host) {
        this(host, MongoDBConstants.DEFAULT_PORT);
    }

    public MongoProcessImpl(Host host, int port) {
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
    public boolean isReady(Optional<Duration> optionalTimeout) {
        try {
            return Util.contains(getClient().listDatabaseNames(), LOCAL_DB_NAME);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Internal error constructing MongoDB client URI", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MongoProcessImpl other = (MongoProcessImpl) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        return true;
    }
}
