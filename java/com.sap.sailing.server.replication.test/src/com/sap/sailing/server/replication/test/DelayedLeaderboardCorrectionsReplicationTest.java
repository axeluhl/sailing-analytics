package com.sap.sailing.server.replication.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

public class DelayedLeaderboardCorrectionsReplicationTest extends AbstractServerReplicationTest {
    private ReplicationService replicaReplicator;
    private ReplicationMasterDescriptor masterDescriptor;
    
    @Before
    public void setUp() throws FileNotFoundException, UnknownHostException, JMSException, Exception {
        Pair<ReplicationService, ReplicationMasterDescriptor> descriptors = basicSetUp();
        replicaReplicator = descriptors.getA();
        masterDescriptor = descriptors.getB();
    }
    
    /**
     * When a leaderboard is loaded from the persistent store, the corrections are keyed by competitor names.
     * These corrections cannot truly be applied to the leaderboard unless a {@link Competitor} object with the
     * name loaded from the store is associated through a {@link TrackedRace} with a {@link RaceColumn} of that
     * leaderboard. Until that happens, there is a {@link DelayedLeaderboardCorrections} object that is attached
     * as a {@link RaceColumnListener} to the leaderboard. It needs to be replicated in the initial load, and
     * tracked race associations to {@link RaceColumn}s need to trigger the resolution of the delayed corrections
     * also in the replica. This is what this test verifies.
     */
    @Test
    public void testDelayedLeaderboardCorrectionsReplication() throws ClassNotFoundException, IOException, JMSException {
        // TODO prepare a leaderboard with corrections on master by creating through RacingEventService which stores it; then
        // create a new RacingEventServiceImpl which is expected to load the leaderboard, but only with DelayedLeaderboardCorrections.
        // then start initial load, wait until finished; then link a tracked race to leaderboard's RaceColumn on master and
        // verify that on the replica it is associated to the RaceColumn as well, and the DelayedLeaderboardCorrections are
        // resolved properly on the replica.
        replicaReplicator.startToReplicateFrom(masterDescriptor);
    }
    
}
