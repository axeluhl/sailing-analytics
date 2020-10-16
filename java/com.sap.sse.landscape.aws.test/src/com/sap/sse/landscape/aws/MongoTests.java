package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.mongodb.impl.MongoProcessImpl;

public class MongoTests {
    private Host localhost;
    private MongoProcessImpl mongoProcess;
    
    @Before
    public void setUp() throws UnknownHostException {
        localhost = Mockito.mock(Host.class);
        Mockito.when(localhost.getPublicAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
        mongoProcess = new MongoProcessImpl(localhost);
    }
    
    @Test
    public void testMongoReadiness() {
        assertTrue(mongoProcess.isReady());
    }
}
