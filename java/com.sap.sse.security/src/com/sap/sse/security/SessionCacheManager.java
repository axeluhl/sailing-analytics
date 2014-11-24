package com.sap.sse.security;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.impl.ReplicatingCache;

/**
 * Obtains the singleton cache manager instance from the singleton {@link SecurityService} instance. The cache manager
 * returned will replicate all modifying operations to the {@link SecurityService} in any replica listening. Using an instance
 * of this class as the cache manager in your Shiro configuration as in
 * 
 * <pre>
 *      sessionDAO = org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO
 *      securityManager.sessionManager.sessionDAO = $sessionDAO
 *      cacheManager = com.sap.sse.security.SessionCacheManager
 *      securityManager.cacheManager = $cacheManager
 * </pre>
 * 
 * will share session state across all other web bundles doing the same and using the same instance of this OSGi bundle.
 * The actual cache manager is initialized as a singleton instance during this bundle's activation.
 * 
 * @see SecurityService#getCacheManager()
 * 
 * @author Benjamin Ebling
 *
 */
public class SessionCacheManager implements CacheManager {
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        final ReplicableSecurityService securityService = (ReplicableSecurityService) Activator.getSecurityService();
        if (securityService == null) {
            return null;
        }
        return new ReplicatingCache<K, V>(securityService, name, securityService.getCacheManager().getCache(name));
    }
}
