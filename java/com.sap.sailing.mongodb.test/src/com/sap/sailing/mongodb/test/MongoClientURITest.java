package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.mongodb.ReadPreference;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class MongoClientURITest {
    @Test
    public void testDefaultConnectionOptions() {
        MongoDBConfiguration config = new MongoDBConfiguration("mongodb://humba:12345/mydb");
        assertEquals("humba", config.getHostname());
        assertEquals(12345, config.getPort());
        assertEquals("mydb", config.getDatabaseName());
        assertSame(ReadPreference.primaryPreferred(), config.getMongoClientURI().getOptions().getReadPreference()); 
    }

    @Test
    public void testSpecificConnectionOptions() {
        MongoDBConfiguration config = new MongoDBConfiguration("mongodb://humba:12345/mydb?replicaset=rs0&readpreference=primary");
        assertEquals("humba", config.getHostname());
        assertEquals(12345, config.getPort());
        assertEquals("mydb", config.getDatabaseName());
        assertSame(ReadPreference.primary(), config.getMongoClientURI().getOptions().getReadPreference()); 
    }
}
