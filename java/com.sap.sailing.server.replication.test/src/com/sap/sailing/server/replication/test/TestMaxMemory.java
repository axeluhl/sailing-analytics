package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestMaxMemory {
    @Test
    public void testMaxMemory() {
        assertTrue(Runtime.getRuntime().maxMemory() > 1000l);
    }
}
