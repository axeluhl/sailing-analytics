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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instances == null) ? 0 : instances.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        MongoReplicaSetImpl other = (MongoReplicaSetImpl) obj;
        if (instances == null) {
            if (other.instances != null)
                return false;
        } else if (!instances.equals(other.instances))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
