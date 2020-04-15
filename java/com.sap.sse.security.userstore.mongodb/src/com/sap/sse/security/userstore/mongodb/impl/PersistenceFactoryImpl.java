package com.sap.sse.security.userstore.mongodb.impl;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.userstore.mongodb.DomainObjectFactory;
import com.sap.sse.security.userstore.mongodb.MongoObjectFactory;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    private final DomainObjectFactory defaultDomainObjectFactory;
    private final MongoObjectFactory defaultMongoObjectFactory;
    
    public PersistenceFactoryImpl() {
        this(MongoDBService.INSTANCE);
    }

    public PersistenceFactoryImpl(MongoDBService mongoDBService) {
        this.defaultDomainObjectFactory = new DomainObjectFactoryImpl(mongoDBService.getDB());
        this.defaultMongoObjectFactory = new MongoObjectFactoryImpl(mongoDBService.getDB());
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
    public MongoObjectFactory getDefaultMongoObjectFactory() {
        return defaultMongoObjectFactory;
    }
}
