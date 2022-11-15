package com.sap.sse.replication.persistence;

import com.sap.sse.replication.ReplicaDescriptor;

public interface DomainObjectFactory {
    Iterable<ReplicaDescriptor> loadReplicaDescriptors();
}
