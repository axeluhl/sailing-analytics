package com.sap.sse.replication.persistence;

import com.sap.sse.replication.ReplicaDescriptor;

public interface MongoObjectFactory {
    void storeReplicaDescriptor(ReplicaDescriptor replicaDescriptor);

    void removeReplicaDescriptor(ReplicaDescriptor replicaDescriptor);
}
