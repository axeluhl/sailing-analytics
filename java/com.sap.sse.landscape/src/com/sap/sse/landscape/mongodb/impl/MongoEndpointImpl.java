package com.sap.sse.landscape.mongodb.impl;

import java.net.URISyntaxException;
import java.util.Optional;

import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.session.ClientSession;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoEndpoint;

public abstract class MongoEndpointImpl implements MongoEndpoint {
    @Override
    public Iterable<MongoDatabase> getMongoDatabases() throws URISyntaxException {
        final MongoClient client = getClient();
        return Util.map(client.listDatabaseNames(), dbName->client.getDatabase(dbName));
    }

    @Override
    public MongoDatabase getMongoDatabase(String dbName) throws URISyntaxException {
        return getClient().getDatabase(dbName);
    }

    @Override
    public MongoDatabase importDatabase(MongoDatabase from) {
        // TODO Implement MongoReplicaSetImpl.importDatabase(...)
        return null;
    }

    @Override
    public boolean isInReplicaSet() throws URISyntaxException {
        return getClient().getReplicaSetStatus() != null;
    }
    
    @Override
    public MongoClientURI getMongoClientURI(Optional<Database> optionalDb) throws URISyntaxException {
        return new MongoClientURI(getURI(optionalDb).toString());
    }
    
    @Override
    public MongoClient getClient() throws URISyntaxException {
        return new MongoClient(getMongoClientURI(Optional.empty()));
    }
    
    @Override
    public ClientSession getClientSession() throws URISyntaxException {
        return getClient().startSession(ClientSessionOptions.builder().causallyConsistent(true).build());
    }

}
