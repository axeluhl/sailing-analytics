package com.sap.sse.security.userstore.mongodb.impl;

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.userstore.mongodb.DomainObjectFactory;
import com.sap.sse.security.userstore.mongodb.MongoObjectFactory;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    private final DomainObjectFactory defaultDomainObjectFactory;
    private final MongoObjectFactory defaultMongoObjectFactory;
    private final DomainObjectFactory defaultMajorityDomainObjectFactory;
    private final MongoObjectFactory defaultMajorityMongoObjectFactory;
    
    public PersistenceFactoryImpl() {
        this(MongoDBService.INSTANCE);
    }

    public PersistenceFactoryImpl(MongoDBService mongoDBService) {
        this.defaultDomainObjectFactory = new DomainObjectFactoryImpl(mongoDBService.getDB());
        this.defaultMongoObjectFactory = new MongoObjectFactoryImpl(mongoDBService.getDB());
        this.defaultMajorityDomainObjectFactory = new DomainObjectFactoryImpl(mongoDBService.getDB().withReadConcern(ReadConcern.MAJORITY).withWriteConcern(WriteConcern.MAJORITY));
        this.defaultMajorityMongoObjectFactory = new MongoObjectFactoryImpl(mongoDBService.getDB().withReadConcern(ReadConcern.MAJORITY).withWriteConcern(WriteConcern.MAJORITY));
    }
    
    /**
     * A default domain object factory for test purposes only. In a server environment, ensure NOT to use this and use
     * {@link #getDefaultMajorityDomainObjectFactory()} instead, using the base domain factory that
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

    @Override
    public DomainObjectFactory getDefaultMajorityDomainObjectFactory() {
        return defaultMajorityDomainObjectFactory;
    }

    @Override
    public MongoObjectFactory getDefaultMajorityMongoObjectFactory() {
        return defaultMajorityMongoObjectFactory;
    }
}
