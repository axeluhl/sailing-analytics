package com.sap.sailing.resultimport.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public class ResultUrlRegistryImpl implements ResultUrlRegistry {
    private final Map<String, Set<URL>> resultUrls;
    private final MongoObjectFactory mongoObjectFactory;
    
    public ResultUrlRegistryImpl(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        this.mongoObjectFactory = mongoObjectFactory;
        resultUrls = domainObjectFactory.loadResultUrls();
    }
    
    @Override
    public void registerResultUrl(String resultProviderName, URL url) {
        Set<URL> urlSet = resultUrls.get(resultProviderName);
        if(urlSet == null) {
            urlSet = new HashSet<URL>();
            resultUrls.put(resultProviderName, urlSet);
        }
        urlSet.add(url);
        mongoObjectFactory.storeResultUrl(resultProviderName, url);
    }

    @Override
    public void unregisterResultUrl(String resultProviderName, URL url) {
        Set<URL> urlSet = resultUrls.get(resultProviderName);
        if(urlSet != null) {
            urlSet.remove(url);
            mongoObjectFactory.removeResultUrl(resultProviderName, url);
        }
    }

    @Override
    public Iterable<URL> getReadableResultUrls(String resultProviderName) {
        final Subject subject = SecurityUtils.getSubject();
        final Iterable<URL> result = getAllResultUrls(resultProviderName);
        for (final Iterator<URL> urlIterator = result.iterator(); urlIterator.hasNext();) {
            final URL url = urlIterator.next();
            if (!subject.isPermitted(SecuredDomainType.RESULT_IMPORT_URL
                    .getStringPermissionForTypeRelativeIdentifier(DefaultActions.READ,
                            new TypeRelativeObjectIdentifier(resultProviderName, url.toString())))) {
                urlIterator.remove();
            }
        }
        return result;
    }

    @Override
    public Iterable<URL> getAllResultUrls(String resultProviderName) {
        final Iterable<URL> result;
        if (resultUrls.containsKey(resultProviderName)) {
            result = new ArrayList<>(resultUrls.get(resultProviderName));
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    @Override
    public Iterable<String> getResultProviderNames() {
        return resultUrls.keySet();
    }
}
