package com.sap.sse.security.impl;

import java.util.Collection;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import com.sap.sse.common.Named;

/**
 * A {@link Cache}s whose modifying operations are replicated. This works because by intercepting the writing operations
 * and running them as a replicable operation on the {@link ReplicableSecurityService}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ReplicatingCache<K, V> implements Cache<K, V>, Named {
    private static final long serialVersionUID = 6628512191363526330L;
    private final ReplicableSecurityService securityService;
    private final String name;
    private final Cache<K, V> cache;

    public ReplicatingCache(ReplicableSecurityService securityService, String name, Cache<K, V> cache) {
        super();
        this.securityService = securityService;
        this.name = name;
        this.cache = cache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public V get(K key) throws CacheException {
        return cache.get(key);
    }

    @Override
    public V put(K key, V value) throws CacheException {
        V result = cache.put(key, value);
        securityService.replicate(s->
            s.getCacheManager().getCache(name).put(key, value));
        return result;
    }

    @Override
    public V remove(K key) throws CacheException {
        V result = cache.remove(key);
        securityService.replicate(s->
            s.getCacheManager().getCache(name).remove(key));
        return result;
    }

    @Override
    public void clear() throws CacheException {
        cache.clear();
        securityService.replicate(s->{ 
            s.getCacheManager().getCache(name).clear(); return null;
        });
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        return cache.keys();
    }

    @Override
    public Collection<V> values() {
        return cache.values();
    }
}
