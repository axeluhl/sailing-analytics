package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class ClearReplicatingCacheOperation
        implements SecurityOperationNotRequiringExplicitTransitiveReplication<Void> {
    private static final long serialVersionUID = -5773023000924245582L;
    private final String cacheName;

    public ClearReplicatingCacheOperation(String cacheName) {
        super();
        this.cacheName = cacheName;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.getCacheManager().getCache(cacheName).clear();
        return null;
    }
}
