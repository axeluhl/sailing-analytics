package com.sap.sse.landscape.mongodb;

import java.net.InetAddress;
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
        return getURI(optionalDb, getHost().getPublicAddress());
    }
    
    @Override
    default URI getURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException {
        final InetAddress publicAddress = getHost().getPublicAddress(timeoutEmptyMeaningForever);
        return getURI(optionalDb, publicAddress);
    }

    default URI getURI(Optional<Database> optionalDb, final InetAddress publicAddress) throws URISyntaxException {
        final StringBuilder sb = new StringBuilder();
        sb.append("mongodb://");
        sb.append(publicAddress.getCanonicalHostName());
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
