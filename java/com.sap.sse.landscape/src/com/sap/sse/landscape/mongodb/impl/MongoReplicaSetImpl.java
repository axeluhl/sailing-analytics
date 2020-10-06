package com.sap.sse.landscape.mongodb.impl;

import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;

public class MongoReplicaSetImpl implements MongoReplicaSet {
    private static final long serialVersionUID = 2055195945588247106L;

    @Override
    public String getName() {
        // TODO Implement MongoReplicaSetImpl.getName(...)
        return null;
    }

    @Override
    public Iterable<MongoProcess> getInstances() {
        // TODO Implement MongoReplicaSetImpl.getInstances(...)
        return null;
    }

    @Override
    public void addReplica(MongoProcess newReplica) {
        // TODO Implement MongoReplicaSetImpl.addReplica(...)

    }

    @Override
    public void removeReplica(MongoProcess replicaToRemove) {
        // TODO Implement MongoReplicaSetImpl.removeReplica(...)

    }

    @Override
    public Iterable<Database> getDatabases() {
        // TODO Implement MongoReplicaSetImpl.getDatabases(...)
        return null;
    }

    @Override
    public Database getDatabase(String dbName) {
        // TODO Implement MongoReplicaSetImpl.getDatabase(...)
        return null;
    }

    @Override
    public Database importDatabase(Database from) {
        // TODO Implement MongoReplicaSetImpl.importDatabase(...)
        return null;
    }

}
