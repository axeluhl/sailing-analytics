package com.sap.sse.landscape.mongodb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface MongoProcess extends Process<RotatingFileBasedLog, MongoMetrics>, MongoEndpoint {
    int DEFAULT_PORT = 27017;
    
    @Override
    default URI getURI(Optional<Database> optionalDb) throws URISyntaxException {
        return getURI(optionalDb, getHostname());
    }
    
    default String getHostname(Optional<Duration> timeoutEmptyMeaningForever) {
        return getHost().getPublicAddress(timeoutEmptyMeaningForever).getCanonicalHostName();
    }
    
    default String getHostname() {
        return getHost().getPublicAddress().getCanonicalHostName();
    }
    
    @Override
    default URI getURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException {
        return getURI(optionalDb, getHostname(timeoutEmptyMeaningForever));
    }

    default URI getURI(Optional<Database> optionalDb, String hostname) throws URISyntaxException {
        final StringBuilder sb = new StringBuilder();
        sb.append("mongodb://");
        sb.append(hostname);
        sb.append(":");
        sb.append(getPort());
        sb.append("/");
        optionalDb.ifPresent(db->sb.append(db.getName()));
        return new URI(sb.toString());
    }
    
    boolean isInReplicaSet() throws URISyntaxException;
    
    /**
     * If not {@link #isInReplicaSet()} then a {@link ClassCastException} will be thrown.
     */
    default MongoProcessInReplicaSet asMongoProcessInReplicaSet() {
        return (MongoProcessInReplicaSet) this;
    }
}
