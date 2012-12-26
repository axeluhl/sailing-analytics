package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceCommitteeStore;
import com.sap.sailing.domain.persistence.MongoRaceCommitteeStoreFactory;

public class MongoRaceCommitteeStoreFactoryImpl implements MongoRaceCommitteeStoreFactory {
	private final DB db;
    
    public MongoRaceCommitteeStoreFactoryImpl(DB db) {
        this.db = db;
    }

	@Override
	public MongoRaceCommitteeStore getMongoRaceCommitteeStore(MongoObjectFactory mongoObjectFactory,DomainObjectFactory domainObjectFactory)
			throws UnknownHostException, MongoException {
		return new MongoRaceCommitteeStoreImpl(db, mongoObjectFactory, domainObjectFactory);
	}

}
