package com.sap.sse.replication.impl;

import java.util.Collections;

import com.sap.sse.replication.Replicable;

public class SingletonReplicablesProvider extends AbstractReplicablesProvider {
    private final Replicable<?, ?> replicable;
    
    public SingletonReplicablesProvider(Replicable<?, ?> replicable) {
        super();
        this.replicable = replicable;
    }

    @Override
    public Iterable<Replicable<?, ?>> getReplicables() {
        return Collections.singleton(replicable);
    }

}
