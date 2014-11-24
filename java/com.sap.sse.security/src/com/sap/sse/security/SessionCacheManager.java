package com.sap.sse.security;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;

import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.ReplicableSecurityService;

/**
 * A {@link CacheManager} implementation that can be used in a <code>shiro.ini</code> configuration for a
 * {@link SecurityManager}. {@link SecurityService#getCacheManager() Obtains} the singleton cache manager instance from
 * the singleton {@link SecurityService} instance. Using an instance of this class as the cache manager in your
 * Shiro configuration as in
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
 * @author Benjamin Ebling, Axel Uhl
 *
 */
public class SessionCacheManager implements CacheManager {
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        final ReplicableSecurityService securityService = (ReplicableSecurityService) Activator.getSecurityService();
        final Cache<K, V> result;
        if (securityService == null) {
            result = null;
        } else {
            result = securityService.getCacheManager().getCache(name);
        }
        return result;
    }
}
