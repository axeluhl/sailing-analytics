package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.subject.PrincipalCollection;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.data.HasSessionContext;
import com.sap.sse.security.datamining.data.impl.SessionWithContext;
import com.sap.sse.security.shared.impl.User;

public class SecuritySessionsRetrievalProcessor extends AbstractRetrievalProcessor<SecurityService, HasSessionContext> {
    public SecuritySessionsRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasSessionContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(SecurityService.class, HasSessionContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasSessionContext> retrieveData(SecurityService securityService) {
        final Set<HasSessionContext> data = new HashSet<>();
        final CacheManager cacheManager = securityService.getCacheManager();
        final Cache<?, ?> activeSessionCache = cacheManager.getCache(CachingSessionDAO.ACTIVE_SESSION_CACHE_NAME);
        for (final Object i : activeSessionCache.values()) {
            if (isAborted()) {
                break;
            }
            if (i instanceof Session) {
                final Session session = (Session) i;
                for (final Object attributeKey : session.getAttributeKeys()) {
                    final Object attributeValue = session.getAttribute(attributeKey);
                    if (attributeValue instanceof PrincipalCollection) {
                        final PrincipalCollection pc = (PrincipalCollection) attributeValue;
                        if (pc != null && pc.getPrimaryPrincipal() != null) {
                            final User user = securityService.getUserByName((String) pc.getPrimaryPrincipal());
                            if (user != null && securityService.hasCurrentUserReadPermission(user)) {
                                data.add(new SessionWithContext(securityService, session, user));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return data;
    }
}
