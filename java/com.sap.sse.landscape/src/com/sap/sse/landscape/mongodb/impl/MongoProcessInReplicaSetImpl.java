package com.sap.sse.landscape.mongodb.impl;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;

public class MongoProcessInReplicaSetImpl extends MongoProcessImpl implements MongoProcessInReplicaSet {
    private final MongoReplicaSet replicaSet;
    
    public MongoProcessInReplicaSetImpl(MongoReplicaSet replicaSet, Host host) {
        super(host);
        this.replicaSet = replicaSet;
    }

    public MongoProcessInReplicaSetImpl(MongoReplicaSet replicaSet, int port, Host host) {
        super(port, host);
        this.replicaSet = replicaSet;
    }

    @Override
    public MongoReplicaSet getReplicaSet() {
        return replicaSet;
    }
}
