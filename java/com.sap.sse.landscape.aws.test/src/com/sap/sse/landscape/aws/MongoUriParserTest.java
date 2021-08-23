package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.mongodb.Database;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.mongodb.MongoProcessInReplicaSet;
import com.sap.sse.landscape.mongodb.MongoReplicaSet;

public class MongoUriParserTest {
    private MongoUriParser parser;
    
    @Before
    public void setUp() {
        parser = new MongoUriParser();
    }
    
    @Test
    public void testSimpleSingleNodeUri() {
        final String hostname = "host.example.com";
        final String dbName = "myDb";
        final Database database = parser.parseMongoUri("mongodb://"+hostname+"/"+dbName);
        assertFalse(database.getEndpoint().isReplicaSet());
        final MongoProcess mongoProcess = database.getEndpoint().asMongoProcess();
        assertEquals(hostname, mongoProcess.getHostname());
        assertEquals(27017, mongoProcess.getPort());
        assertEquals(dbName, database.getName());
    }

    @Test
    public void testSimpleSingleNodeUriWithExplicitPort() {
        final String hostname = "host.example.com";
        final int port = 10202;
        final String dbName = "myDb";
        final Database database = parser.parseMongoUri("mongodb://"+hostname+":"+port+"/"+dbName);
        assertFalse(database.getEndpoint().isReplicaSet());
        final MongoProcess mongoProcess = database.getEndpoint().asMongoProcess();
        assertEquals(hostname, mongoProcess.getHostname());
        assertEquals(port, mongoProcess.getPort());
        assertEquals(dbName, database.getName());
    }

    @Test
    public void testExceptionForSingleNodeUriWithoutDatabaseName() {
        final String hostname = "host.example.com";
        try {
            parser.parseMongoUri("mongodb://"+hostname);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test
    public void testSimpleReplicaSetUri() {
        final String replicaSetName = "humba";
        final String hostname1 = "host1.example.com";
        final String hostname2 = "127.0.0.1";
        final String dbName = "myDb";
        final Database database = parser.parseMongoUri("mongodb://"+hostname1+","+hostname2+"/"+dbName+"?retryWrites=true&replicaSet="+replicaSetName+"&readPreference=nearest");
        assertTrue(database.getEndpoint().isReplicaSet());
        final MongoReplicaSet mongoReplicaSet = database.getEndpoint().asMongoReplicaSet();
        boolean foundHost1 = false;
        boolean foundHost2 = false;
        for (final MongoProcessInReplicaSet instance : mongoReplicaSet.getInstances()) {
            if (Util.equalsWithNull(hostname1, instance.getHostname())) {
                foundHost1 = true;
            }
            if (Util.equalsWithNull(hostname2, instance.getHostname())) {
                foundHost2 = true;
            }
            assertEquals(27017, instance.getPort());
        }
        assertTrue(foundHost1);
        assertTrue(foundHost2);
        assertEquals(dbName, database.getName());
    }

    @Test
    public void testSimpleReplicaSetUriWithExplicitPort() {
        final String replicaSetName = "humba";
        final String hostname1 = "host1.example.com";
        int port1 = 12345;
        final String hostname2 = "127.0.0.1";
        final String dbName = "myDb";
        final Database database = parser.parseMongoUri("mongodb://"+hostname1+":"+port1+","+hostname2+"/"+dbName+"?replicaSet="+replicaSetName+"&retryWrites=true&readPreference=nearest");
        assertTrue(database.getEndpoint().isReplicaSet());
        final MongoReplicaSet mongoReplicaSet = database.getEndpoint().asMongoReplicaSet();
        boolean foundHost1 = false;
        boolean foundHost2 = false;
        for (final MongoProcessInReplicaSet instance : mongoReplicaSet.getInstances()) {
            if (Util.equalsWithNull(hostname1, instance.getHostname())) {
                foundHost1 = true;
                assertEquals(port1, instance.getPort());
            }
            if (Util.equalsWithNull(hostname2, instance.getHostname())) {
                foundHost2 = true;
                assertEquals(27017, instance.getPort());
            }
        }
        assertTrue(foundHost1);
        assertTrue(foundHost2);
        assertEquals(dbName, database.getName());
    }

    @Test
    public void testExceptionForReplicaSetUriWithoutDatabaseName() {
        final String replicaSetName = "humba";
        final String hostname1 = "host1.example.com";
        int port1 = 12345;
        final String hostname2 = "127.0.0.1";
        try {
            parser.parseMongoUri("mongodb://"+hostname1+":"+port1+","+hostname2+"?replicaSet="+replicaSetName+"&retryWrites=true&readPreference=nearest");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
