package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class RemoveFromReplicatingCacheOperation<K>
        implements SecurityOperationNotRequiringExplicitTransitiveReplication<Object> {
    private static final long serialVersionUID = -1754187358645381535L;
    private final String cacheName;
    private final K key;
    
    public RemoveFromReplicatingCacheOperation(String cacheName, K key) {
        super();
        this.cacheName = cacheName;
        this.key = key;
    }

    @Override
    public Object internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.getCacheManager().getCache(cacheName).remove(key);
    }

}
