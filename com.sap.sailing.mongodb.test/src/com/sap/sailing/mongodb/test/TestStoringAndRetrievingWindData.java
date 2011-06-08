package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class TestStoringAndRetrievingWindData {
    private static final String WIND_TEST_DB = "wind_test_db";
    private static final String WIND_TEST_COLLECTION = "wind_test_collection";

    @Test
    public void testDBConnection() throws UnknownHostException, MongoException {
        Mongo mongo = new Mongo();
        assertNotNull(mongo);
        DB db = mongo.getDB(WIND_TEST_DB);
        assertNotNull(db);
        DBCollection coll = db.getCollection(WIND_TEST_COLLECTION);
        assertNotNull(coll);
        BasicDBObject doc = new BasicDBObject();
        doc.put("truebearingdeg", 234.3);
        doc.put("knotspeed", 10.7);
        coll.insert(doc);
    }

    @Test
    public void testDBRead() throws UnknownHostException, MongoException {
        Mongo mongo = new Mongo();
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
