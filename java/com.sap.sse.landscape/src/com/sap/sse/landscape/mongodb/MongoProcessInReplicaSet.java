package com.sap.sse.landscape.mongodb;

public interface MongoProcessInReplicaSet extends MongoProcess {
    MongoReplicaSet getReplicaSet();
}
