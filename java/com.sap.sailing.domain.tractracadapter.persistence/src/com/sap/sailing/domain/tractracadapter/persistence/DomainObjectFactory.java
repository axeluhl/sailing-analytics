package com.sap.sailing.domain.tractracadapter.persistence;

import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.MongoDBService;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl(MongoDBService.INSTANCE.getDB());

    Iterable<TracTracConfiguration> getTracTracConfigurations();
    
}
