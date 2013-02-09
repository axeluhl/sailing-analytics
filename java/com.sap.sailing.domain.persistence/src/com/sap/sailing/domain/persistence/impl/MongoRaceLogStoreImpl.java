package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStore;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;

public class MongoRaceLogStoreImpl extends EmptyRaceLogStore implements MongoRaceLogStore {
	private transient final DB db;
	private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    
    public MongoRaceLogStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory)
            throws UnknownHostException, MongoException {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
    }

	@Override
	public RaceLog getRaceLog(RaceColumnIdentifier identifier) {
		RaceLog track;
		track = domainObjectFactory.loadRaceLog(identifier);
		track.addListener(new MongoRaceLogListener(identifier, mongoObjectFactory, db));
		return track;
	}

}
