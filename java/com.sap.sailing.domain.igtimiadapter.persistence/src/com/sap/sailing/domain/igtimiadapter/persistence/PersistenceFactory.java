package com.sap.sailing.domain.igtimiadapter.persistence;

import com.sap.sailing.mongodb.MongoDBService;

public interface PersistenceFactory {
    DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDbService);
    DomainObjectFactory getMongoObjectFactory(MongoDBService mongoDbService);
}
