package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class PutToReplicatingCacheOperation<K, V>
        implements SecurityOperationNotRequiringExplicitTransitiveReplication<Object> {
    private static final long serialVersionUID = -9018465352648434564L;
    private final String cacheName;
    private final K key;
    private final V value;
    
    public PutToReplicatingCacheOperation(String cacheName, K key, V value) {
        super();
        this.cacheName = cacheName;
        this.key = key;
        this.value = value;
    }

    @Override
    public Object internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.getCacheManager().getCache(cacheName).put(key, value);
    }

}
