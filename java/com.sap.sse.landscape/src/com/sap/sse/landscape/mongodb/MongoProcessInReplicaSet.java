package com.sap.sse.landscape.mongodb;

public interface MongoProcessInReplicaSet extends MongoProcess {
    boolean isHidden();

    int getPriority();

    int getVotes();
}
