package com.sap.sailing.domain.persistence.impl;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.mongodb.MongoDBService;

public class PersistenceFactoryImpl implements PersistenceFactory {
    private final DomainObjectFactory defaultDomainObjectFactory;
    private final MongoObjectFactory defaultMongoObjectFactory;
    
    public PersistenceFactoryImpl() {
        super();
        this.defaultDomainObjectFactory = new DomainObjectFactoryImpl(MongoDBService.INSTANCE.getDB(), DomainFactory.INSTANCE);
        this.defaultMongoObjectFactory = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
    }

    /**
     * A default domain object factory for test purposes only. In a server environment, ensure NOT to use this and use
     * {@link #getDomainObjectFactory(MongoDBService, DomainFactory)} instead, using the base domain factory that
     * is provided by <code>RacingEventService.getBaseDomainFactory</code>.
     */
    @Override
    public DomainObjectFactory getDefaultDomainObjectFactory() {
        return defaultDomainObjectFactory;
    }

    @Override
    public DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDBService, DomainFactory baseDomainFactory) {
        return new DomainObjectFactoryImpl(mongoDBService.getDB(), baseDomainFactory);
    }

    @Override
    public MongoObjectFactory getDefaultMongoObjectFactory() {
        return defaultMongoObjectFactory;
    }

    @Override
    public MongoObjectFactory getMongoObjectFactory(MongoDBService mongoDBService) {
        return new MongoObjectFactoryImpl(mongoDBService.getDB());
    }

}
