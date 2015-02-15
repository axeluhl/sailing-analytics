package com.sap.sse.replication.testsupport;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractServerWithMultipleServicesReplicationTest {
    protected final Set<AbstractServerReplicationTestSetUp<?, ?>> testSetUps = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        for (AbstractServerReplicationTestSetUp<?, ?> testSetUp : testSetUps) {
            testSetUp.setUp();
        }
    }
    
    @After
    public void tearDown() throws Exception {
        for (AbstractServerReplicationTestSetUp<?, ?> testSetUp : testSetUps) {
            testSetUp.tearDown();
        }
    }
}
