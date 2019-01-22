package com.sap.sse.security.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;

import com.sap.sse.common.Named;

/**
 * A {@link Cache}s whose modifying operations are replicated. This works by intercepting the writing operations
 * and running them as a replicable operation on the {@link ReplicableSecurityService}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ReplicatingCache<K, V> implements Cache<K, V>, Named {
    private static final Logger logger = Logger.getLogger(ReplicatingCache.class.getName());
    private static final long serialVersionUID = 6628512191363526330L;
    private transient ReplicableSecurityService securityService;
    private final String name;
    private final ConcurrentMap<K, V> cache;

    public ReplicatingCache(ReplicableSecurityService securityService, String name) {
        super();
        this.securityService = securityService;
        this.name = name;
        this.cache = new ConcurrentHashMap<K, V>();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        securityService = (ReplicableSecurityService) Activator.getSecurityService();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public V get(K key) throws CacheException {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("get("+key+") on "+name+"@"+System.identityHashCode(this)+"="+cache.get(key));
        }
        return cache.get(key);
    }

    @Override
    public V put(K key, V value) throws CacheException {
        return put(key, value, /* store */ true);
    }

    public V put(K key, V value, boolean store) throws CacheException {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("put("+key+", "+value+") into cache "+name+"@"+System.identityHashCode(this));
            if (value instanceof Session) {
                Session session = (Session) value;
                logSession(session);
            }
        }
        V result = cache.put(key, value);
        final String myName = name;
        securityService.replicate(s->
            s.getCacheManager().getCache(myName).put(key, value));
        if (store && value instanceof Session) {
            securityService.storeSession(getName(), (Session) value);
        }
        return result;
    }

    private void logSession(Session session) {
        final StringBuilder sb = new StringBuilder();
        for (Object key : session.getAttributeKeys()) {
            sb.append("  ");
            sb.append(key);
            sb.append("=");
            sb.append(session.getAttribute(key));
            sb.append("\n");
        }
        logger.finer("Session:\n"+sb.toString());
        
    }

    @Override
    public V remove(K key) throws CacheException {
        V result = cache.remove(key);
        final String myName = name;
        securityService.replicate(s->
            s.getCacheManager().getCache(myName).remove(key));
        if (result instanceof Session) {
            securityService.removeSession(getName(), (Session) result);
        }
        return result;
    }

    @Override
    public void clear() throws CacheException {
        cache.clear();
        final String myName = name;
        securityService.replicate(s->{ 
            s.getCacheManager().getCache(myName).clear(); return null;
        });
        securityService.removeAllSessions(getName());
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        return cache.keySet();
    }

    @Override
    public Collection<V> values() {
        return cache.values();
    }
}
