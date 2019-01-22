package com.sap.sse.security.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

import com.sap.sse.security.persistence.PersistenceFactory;

/**
 * Wraps a {@link CacheManager} and makes all modifying operations replicate their changes and store them persistently.
 * This happens by wrapping all {@link Cache}s by an object that intercepts the writing operations, producing
 * replicating operations.
 * 
 * @see PersistenceFactory
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ReplicatingCacheManager implements CacheManager, Serializable {
    private static final long serialVersionUID = -8035346668009900228L;
    private ConcurrentMap<String, ReplicatingCache<?, ?>> caches;
    
    public ReplicatingCacheManager() {
        this.caches = new ConcurrentHashMap<>();
    }
    
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        final ReplicableSecurityService securityService = (ReplicableSecurityService) Activator.getSecurityService();
        if (securityService == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        ReplicatingCache<K, V> cache = (ReplicatingCache<K, V>) this.caches.get(name);
        if (cache == null) {
            cache = new ReplicatingCache<K, V>(securityService, name);
            caches.put(name, cache);
        }
        return cache;
    }
    
    public void replaceContentsFrom(ReplicatingCacheManager other) {
        for (Entry<String, ReplicatingCache<?, ?>> i : other.caches.entrySet()) {
            replaceOrUpdate(i.getKey(), i.getValue());
        }
    }

    private <K, V> void replaceOrUpdate(String name, ReplicatingCache<K, V> otherCache) {
        @SuppressWarnings("unchecked")
        ReplicatingCache<K, V> castCache = (ReplicatingCache<K, V>) this.caches.get(name);
        if (castCache == null) {
            this.caches.put(name, otherCache);
        } else {
            putAll(otherCache, castCache);
        }
    }

    private <K, V> void putAll(ReplicatingCache<K, V> from, final ReplicatingCache<K, V> to) {
        to.clear();
        for (K k : from.keys()) {
            to.put(k, from.get(k));
        }
    }
    
    /**
     * For test purposes; clears all state held by this cache manager.
     */
    public void clear() {
        final ReplicableSecurityService securityService = (ReplicableSecurityService) Activator.getSecurityService();
        for (final Iterator<Entry<String, ReplicatingCache<?, ?>>> i=caches.entrySet().iterator(); i.hasNext(); ) {
            final Entry<String, ReplicatingCache<?, ?>> cacheNameAndCache = i.next();
            securityService.removeAllSessions(cacheNameAndCache.getKey());
            i.remove();
        }
    }
}
