package com.sap.sailing.server.replication.test;

import org.junit.Test;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class ServerReplicationTest {
    @Test
    public void testEmptyInitialLoad() {
        RacingEventService master = new RacingEventServiceImpl();
        // does nothing but avoid a Maven / Surefire error message
    }
}
