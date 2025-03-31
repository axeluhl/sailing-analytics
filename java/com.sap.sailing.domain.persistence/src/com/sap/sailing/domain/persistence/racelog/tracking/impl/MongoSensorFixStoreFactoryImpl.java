package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoSensorFixStore;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoSensorFixStoreFactory;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

public class MongoSensorFixStoreFactoryImpl implements MongoSensorFixStoreFactory {	
    @Override
    public MongoSensorFixStore getMongoGPSFixStore(MongoObjectFactory mongoObjectFactory,
            DomainObjectFactory domainObjectFactory, TypeBasedServiceFinderFactory serviceFinderFactory)
                    throws UnknownHostException, MongoException {
        return new MongoSensorFixStoreImpl(mongoObjectFactory, domainObjectFactory, serviceFinderFactory);
    }

    @Override
    public MongoSensorFixStore getMongoGPSFixStore(MongoObjectFactory mongoObjectFactory,
            DomainObjectFactory domainObjectFactory, TypeBasedServiceFinderFactory serviceFinderFactory,
            ReadConcern readConcern, WriteConcern writeConcern) throws UnknownHostException, MongoException {
        return getMongoGPSFixStore(mongoObjectFactory, domainObjectFactory, serviceFinderFactory, readConcern,
                writeConcern, /* clientSession */ null, /* metadataCollectionClientSession */ null);
    }

    @Override
    public MongoSensorFixStore getMongoGPSFixStore(MongoObjectFactory mongoObjectFactory,
            DomainObjectFactory domainObjectFactory, TypeBasedServiceFinderFactory serviceFinderFactory,
            ReadConcern readConcern, WriteConcern writeConcern, ClientSession clientSession,
            ClientSession metadataCollectionClientSession) throws UnknownHostException, MongoException {
        return new MongoSensorFixStoreImpl(mongoObjectFactory, domainObjectFactory, serviceFinderFactory, readConcern,
                writeConcern, clientSession, metadataCollectionClientSession);
    }
}
