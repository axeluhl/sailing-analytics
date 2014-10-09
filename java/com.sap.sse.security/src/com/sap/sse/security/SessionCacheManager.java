package com.sap.sse.security;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * Obtains the singleton cache manager instance from the singleton {@link SecurityService} instance.
 * 
 * @see SecurityService#getCacheManager()
 * 
 * @author Benjamin Ebling
 *
 */
public class SessionCacheManager implements CacheManager {
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        final SecurityService securityService = Activator.getSecurityService();
        if (securityService == null){
            return null;
        }
        return securityService.getCacheManager().getCache(name);
    }
}
