package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalog.RegattaLogStore;

public class MongoRegattaLogStoreImpl implements RegattaLogStore {
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final Map<RegattaLikeIdentifier, RegattaLog> regattaLogCache;

    public MongoRegattaLogStoreImpl(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.regattaLogCache = new HashMap<>();
    }

    @Override
    public RegattaLog getRegattaLog(RegattaLikeIdentifier identifier, boolean ignoreCache) {
        final RegattaLog result;
        if (!ignoreCache && regattaLogCache.containsKey(identifier)) {
            result = regattaLogCache.get(identifier);
        } else {
            result = domainObjectFactory.loadRegattaLog(identifier);
            MongoRegattaLogStoreVisitor listener = new MongoRegattaLogStoreVisitor(identifier, mongoObjectFactory);
            result.addListener(listener);
            regattaLogCache.put(identifier, result);
        }
        return result;
    }

    @Override
    public void removeRegattaLog(RegattaLikeIdentifier identifier) {
        regattaLogCache.remove(identifier);
        mongoObjectFactory.removeRegattaLog(identifier);
    }
}
