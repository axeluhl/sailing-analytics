package com.sap.sse.landscape.mongodb.impl;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;

public class MongoProcessInReplicaSetImpl extends MongoProcessImpl implements MongoProcessInReplicaSet {
    public MongoProcessInReplicaSetImpl(Host host) {
        super(host);
    }

    public MongoProcessInReplicaSetImpl(int port, Host host) {
        super(port, host);
    }
    
    @Override
    public boolean isHidden() {
        // TODO Implement MongoProcessImpl.isHidden(...)
        return false;
    }

    @Override
    public int getPriority() {
        // TODO Implement MongoProcessImpl.getPriority(...)
        return 0;
    }

    @Override
    public int getVotes() {
        // TODO Implement MongoProcessImpl.getVotes(...)
        return 0;
    }

    @Override
    public boolean isInReplicaSet() {
        return true;
    }
}
