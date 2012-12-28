package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventFactory;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;

public class TestStoringAndRetrievingRaceCommitteeEventData extends AbstractMongoDBTest {
	private static final String RACECOMMITTEE_TEST_COLLECTION = "racecommittee_test_collection";

	public TestStoringAndRetrievingRaceCommitteeEventData() throws UnknownHostException, MongoException {
		super();
	}
	
	@Before
	@Override
	public void dropTestDB() throws UnknownHostException, MongoException, InterruptedException {
		super.dropTestDB();
		db.getCollection(RACECOMMITTEE_TEST_COLLECTION).drop();
	}
	
	@Test
    public void testDBConnection() throws UnknownHostException, MongoException {
        DBCollection coll = db.getCollection(RACECOMMITTEE_TEST_COLLECTION);
        assertNotNull(coll);
        BasicDBObject doc = new BasicDBObject();
        doc.put("upperflag", "AP");
        doc.put("lowerflag", "ALPHA");
        coll.insert(doc);
    }
	
	@Test
    public void testDBRead() throws UnknownHostException, MongoException, InterruptedException {
        {
            DBCollection coll = db.getCollection(RACECOMMITTEE_TEST_COLLECTION);
            assertNotNull(coll);
            BasicDBObject doc = new BasicDBObject();
            doc.put("upperflag", "AP");
            doc.put("lowerflag", "ALPHA");
            coll.insert(doc);
        }

        {
            DBCollection coll = db.getCollection(RACECOMMITTEE_TEST_COLLECTION);
            assertNotNull(coll);
            DBObject object = coll.findOne();
            assertEquals("AP", object.get("upperflag"));
            assertEquals("ALPHA", object.get("lowerflag"));
        }
    }
	
	@Test
    public void storeWindObject() throws UnknownHostException, MongoException, InterruptedException {
        TimePoint now = MillisecondsTimePoint.now();
        RaceCommitteeFlagEvent rcEvent = RaceCommitteeEventFactory.INSTANCE.createFlagEvent(now, 0, Flags.AP, Flags.ALPHA, true);
        {
            DBObject rcEventForMongo = ((MongoObjectFactoryImpl) MongoFactory.INSTANCE.getDefaultMongoObjectFactory())
            		.storeRaceCommitteeFlagEvent(rcEvent);
            DBCollection coll = db.getCollection(RACECOMMITTEE_TEST_COLLECTION);
            coll.insert(rcEventForMongo);
        }
        
        {
            DBCollection coll = db.getCollection(RACECOMMITTEE_TEST_COLLECTION);
            assertNotNull(coll);
            DBObject object = coll.findOne();
            RaceCommitteeFlagEvent readRcEvent = (RaceCommitteeFlagEvent) ((DomainObjectFactoryImpl) MongoFactory.INSTANCE.getDefaultDomainObjectFactory()).loadRaceCommitteeEvent(object);
            assertEquals(rcEvent.getTimePoint(), readRcEvent.getTimePoint());
            assertEquals(rcEvent.getId(), readRcEvent.getId());
            assertEquals(rcEvent.getPassId(), readRcEvent.getPassId());
            assertEquals(rcEvent.getUpperFlag(), readRcEvent.getUpperFlag());
            assertEquals(rcEvent.getLowerFlag(), readRcEvent.getLowerFlag());
        }
    }

}
