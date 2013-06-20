package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.server.operationaltransformation.CreateLeaderboardGroup;
import com.sap.sailing.server.replication.impl.ReplicaDescriptor;
import com.sap.sailing.server.replication.impl.ReplicationInstancesManager;

public class ReplicationInstancesManagerLoggingPerformanceTest {
    private ReplicationInstancesManager replicationInstanceManager;
    private CreateLeaderboardGroup operation;
    private ReplicaDescriptor replica;

    @Before
    public void setUp() throws UnknownHostException {
        replicationInstanceManager = new ReplicationInstancesManager();
        replica = new ReplicaDescriptor(InetAddress.getLocalHost());
        replicationInstanceManager.registerReplica(replica);
        operation = new CreateLeaderboardGroup("Test Leaderboard Group", "Description of Test Leaderboard Group", /* displayGroupsInReverseOrder */ false, Arrays.asList(new String[] { "Default Leaderboard" }),
                /* overallLeaderboardDiscardThresholds */ null, /* overallLeaderboardScoringSchemeType */ null);
    }
    
    @Test
    public void testLoggingPerformance() {
        final int count = 10000000;
        for (int i=0; i<count; i++) {
            replicationInstanceManager.log(operation);
        }
        assertEquals(count, replicationInstanceManager.getStatistics(replica).get(operation.getClass()).intValue());
    }
    
}
