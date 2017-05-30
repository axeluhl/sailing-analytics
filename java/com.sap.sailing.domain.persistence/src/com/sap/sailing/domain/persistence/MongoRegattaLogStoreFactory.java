package com.sap.sailing.domain.persistence;


import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.persistence.impl.MongoRegattaLogStoreFactoryImpl;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalog.RegattaLogStore;

public interface MongoRegattaLogStoreFactory {
    MongoRegattaLogStoreFactory INSTANCE = new MongoRegattaLogStoreFactoryImpl();

    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
    RegattaLogStore getMongoRegattaLogStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory);
    
    RegattaLogEventVisitor getMongoRegattaLogStoreVisitor(RegattaLikeIdentifier identifier, MongoObjectFactory mongoObjectFactory);
}
