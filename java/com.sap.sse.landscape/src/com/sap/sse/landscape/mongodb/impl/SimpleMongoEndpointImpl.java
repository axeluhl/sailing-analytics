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
}
