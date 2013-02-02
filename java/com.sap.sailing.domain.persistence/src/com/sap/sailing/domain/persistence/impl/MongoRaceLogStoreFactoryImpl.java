package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStore;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;

public class MongoRaceLogStoreFactoryImpl implements MongoRaceLogStoreFactory {
	private final DB db;
    
    public MongoRaceLogStoreFactoryImpl(DB db) {
        this.db = db;
    }

	@Override
	public MongoRaceLogStore getMongoRaceLogStore(MongoObjectFactory mongoObjectFactory,DomainObjectFactory domainObjectFactory)
			throws UnknownHostException, MongoException {
		return new MongoRaceLogStoreImpl(db, mongoObjectFactory, domainObjectFactory);
	}

}
