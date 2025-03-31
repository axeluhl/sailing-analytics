package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface MongoProcess extends Process<RotatingFileBasedLog, MongoMetrics>, MongoEndpoint {
    @Override
    default URI getURI(Optional<Database> optionalDb) throws URISyntaxException {
        return getURI(optionalDb, getHostname(), getPort(), /* no replica set name */ Optional.empty());
    }
    
    @Override
    default URI getURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException {
        return getURI(optionalDb, getHostname(timeoutEmptyMeaningForever), getPort(), /* no replica set name */ Optional.empty());
    }

    boolean isInReplicaSet() throws URISyntaxException;
    
    /**
     * If not {@link #isInReplicaSet()} then a {@link ClassCastException} will be thrown.
     */
    default MongoProcessInReplicaSet asMongoProcessInReplicaSet() {
        return (MongoProcessInReplicaSet) this;
    }
}
