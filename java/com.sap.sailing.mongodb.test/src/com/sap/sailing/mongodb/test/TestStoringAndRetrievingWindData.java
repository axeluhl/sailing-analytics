package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.mongodb.DomainObjectFactory;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

public class TestStoringAndRetrievingWindData implements MongoDBTest {
    private static final String WIND_TEST_COLLECTION = "wind_test_collection";
    private Mongo mongo;
    private DB db;
    
    private Mongo newMongo() throws UnknownHostException, MongoException {
        return new Mongo(System.getProperty("mongo.host", "127.0.0.1"),
                ((MongoWindStoreFactoryImpl) MongoWindStoreFactoryImpl.getDefaultInstance()).getDefaultPort());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException {
        mongo = newMongo();
        assertNotNull(mongo);
        mongo.dropDatabase(WIND_TEST_DB);
        db = mongo.getDB(WIND_TEST_DB);
        assertNotNull(db);
    }
    
    @Test
    public void testDBConnection() throws UnknownHostException, MongoException {
        DBCollection coll = db.getCollection(WIND_TEST_COLLECTION);
        assertNotNull(coll);
        BasicDBObject doc = new BasicDBObject();
        doc.put("truebearingdeg", 234.3);
        doc.put("knotspeed", 10.7);
        coll.insert(doc);
    }

    @Test
    public void testDBRead() throws UnknownHostException, MongoException, InterruptedException {
        {
            DBCollection coll = db.getCollection(WIND_TEST_COLLECTION);
            assertNotNull(coll);
            BasicDBObject doc = new BasicDBObject();
            doc.put("truebearingdeg", 234.3);
            doc.put("knotspeed", 10.7);
            coll.insert(doc);
        }

        {
            Thread.sleep(1000); // wait until MongoDB has recorded the change and made it visible
            Mongo mongo = newMongo();
            assertNotNull(mongo);
            DB db = mongo.getDB(WIND_TEST_DB);
            assertNotNull(db);
            DBCollection coll = db.getCollection(WIND_TEST_COLLECTION);
            assertNotNull(coll);
            DBObject object = coll.findOne();
            assertEquals(object.get("truebearingdeg"), 234.3);
            assertEquals(object.get("knotspeed"), 10.7);
        }
    }
    
    @Test
    public void storeWindObject() throws UnknownHostException, MongoException, InterruptedException {
        TimePoint now = MillisecondsTimePoint.now();
        Wind wind = new WindImpl(new DegreePosition(123, 45), now, new KnotSpeedWithBearingImpl(10.4,
                new DegreeBearingImpl(355.5)));
        {
            DBObject windForMongo = ((MongoObjectFactoryImpl) MongoObjectFactory.INSTANCE).storeWind(wind);
            DBCollection coll = db.getCollection(WIND_TEST_COLLECTION);
            coll.insert(windForMongo);
        }
        
        {
            Thread.sleep(1000); // wait until MongoDB has recorded the change and made it visible
            Mongo mongo = newMongo();
            assertNotNull(mongo);
            DB db = mongo.getDB(WIND_TEST_DB);
            assertNotNull(db);
            DBCollection coll = db.getCollection(WIND_TEST_COLLECTION);
            assertNotNull(coll);
            DBObject object = coll.findOne();
            Wind readWind = ((DomainObjectFactoryImpl) DomainObjectFactory.INSTANCE).loadWind(object);
            assertEquals(wind.getPosition(), readWind.getPosition());
            assertEquals(wind.getKnots(), readWind.getKnots(), 0.00000001);
            assertEquals(wind.getBearing().getDegrees(), readWind.getBearing().getDegrees(), 0.00000001);
        }
    }
}
