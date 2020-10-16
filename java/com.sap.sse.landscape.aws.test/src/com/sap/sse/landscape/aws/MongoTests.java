package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.mongodb.impl.MongoProcessImpl;
import com.sap.sse.landscape.mongodb.impl.MongoProcessInReplicaSetImpl;
import com.sap.sse.landscape.mongodb.impl.MongoReplicaSetImpl;

public class MongoTests {
    private Host localhost;
    private MongoProcessImpl mongoProcess;
    private MongoProcessInReplicaSetImpl mongoProcessInReplicaSet;
    private MongoReplicaSetImpl mongoReplicaSet;
    
    @Before
    public void setUp() throws UnknownHostException {
        localhost = Mockito.mock(Host.class);
        Mockito.when(localhost.getPublicAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        mongoProcess = new MongoProcessImpl(localhost);
        mongoReplicaSet = new MongoReplicaSetImpl("rs0");
        mongoReplicaSet.addReplica(mongoProcess);
        mongoProcessInReplicaSet = new MongoProcessInReplicaSetImpl(mongoReplicaSet, 10222, localhost);
    }
    
    @Test
    public void testMongoReadiness() {
        assertTrue(mongoProcess.isReady());
    }
    
    @Test
    public void testMongoProcessInReplicaSetIsAvailable() {
        assertTrue(mongoProcessInReplicaSet.isReady());
    }
    
    @Test
    public void testMongoProcessInReplicaSetCanReportPriority() throws URISyntaxException {
        assertTrue(mongoProcessInReplicaSet.isInReplicaSet());
    }
}
