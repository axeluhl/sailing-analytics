package com.sap.sse.landscape.mongodb.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.mongodb.Database;

public class SimpleMongoEndpointImpl extends MongoEndpointImpl {
    private final String hostname;
    private final int port;
    private final Optional<String> replicaSetName;
    
    public SimpleMongoEndpointImpl(String hostname, int port) {
        this(hostname, port, /* no replica set name */ null);
    }
    
    public SimpleMongoEndpointImpl(String hostname, int port, String replicaSetName) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.replicaSetName = Optional.ofNullable(replicaSetName);
    }

    @Override
    public URI getURI(Optional<Database> optionalDb) throws URISyntaxException {
        return getURI(optionalDb, hostname, port, replicaSetName);
    }

    @Override
    public URI getURI(Optional<Database> optionalDb, Optional<Duration> timeoutEmptyMeaningForever) throws URISyntaxException {
        return getURI(optionalDb);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + port;
        result = prime * result + ((replicaSetName == null) ? 0 : replicaSetName.hashCode());
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
        SimpleMongoEndpointImpl other = (SimpleMongoEndpointImpl) obj;
        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;
        if (port != other.port)
            return false;
        if (replicaSetName == null) {
            if (other.replicaSetName != null)
                return false;
        } else if (!replicaSetName.equals(other.replicaSetName))
            return false;
        return true;
    }
}
