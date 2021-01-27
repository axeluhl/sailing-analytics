package com.sap.sse.landscape.mongodb.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;

public class MongoReplicaSetImpl extends MongoEndpointImpl implements MongoReplicaSet {
    private static final long serialVersionUID = 2055195945588247106L;
    private final String name;
    private final Set<MongoProcessInReplicaSet> instances;
    
    public MongoReplicaSetImpl(String name) {
        super();
        this.name = name;
        this.instances = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterable<MongoProcessInReplicaSet> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

    @Override
    public void addReplica(MongoProcessInReplicaSet newReplica) {
        instances.add(newReplica);
    }

    @Override
    public void removeReplica(MongoProcessInReplicaSet replicaToRemove) {
        instances.remove(replicaToRemove);
    }
}
