package com.sap.sse.security.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * Wraps a {@link CacheManager} and makes all modifying operations replicate their changes. This happens by wrapping
 * all {@link Cache}s by an object that intercepts the writing operations, producing replicating operations.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ReplicatingCacheManager implements CacheManager, Serializable {
    private static final long serialVersionUID = -8035346668009900228L;
    private ConcurrentHashMap<String, ConcurrentHashMap<Object, Object>> caches;
    
    public ReplicatingCacheManager() {
        this.caches = new ConcurrentHashMap<>();
    }
    
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        final ReplicableSecurityService securityService = (ReplicableSecurityService) Activator.getSecurityService();
        if (securityService == null) {
            return null;
        }
        ConcurrentHashMap<Object, Object> cache = this.caches.get(name);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            caches.put(name, cache);
        }
        @SuppressWarnings("unchecked")
        final Map<K, V> castCache = (Map<K, V>) cache;
        ReplicatingCache<K, V> result = new ReplicatingCache<K, V>(securityService, name, castCache);
        return result;
    }
    
    public void replaceContentsFrom(ReplicatingCacheManager other) {
        this.caches = other.caches;
    }
}
