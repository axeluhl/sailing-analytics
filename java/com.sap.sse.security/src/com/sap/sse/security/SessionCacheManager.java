package com.sap.sse.security;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

public class SessionCacheManager implements CacheManager {
    
    SecurityService securityService;

    
    public SessionCacheManager() {
        securityService = Activator.getSecurityService();
    }


    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        if (securityService == null){
            return null;
        }
        return securityService.getCacheManager().getCache(name);
    }

    
}
