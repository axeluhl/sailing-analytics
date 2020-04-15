package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.server.operationaltransformation.AllowBoatResetToDefaults;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateBoat;

/**
 * Tests replication of boats in conjunction with the {@link CompetitorAndBoatStore} concepts, particularly the
 * possibility to allow for boat data to be updated, either explicitly or implicitly from a tracking provider
 * after marking the boat using
 * {@link CompetitorAndBoatStore#allowBoatResetToDefaults(com.sap.sailing.domain.base.Boat)}.
 * 
 * @author Frank Mittag
 * 
 */
public class PostingOperationFromReplicaToMasterTest extends AbstractServerReplicationTest {
    @Rule public Timeout timeout = new Timeout(30, TimeUnit.SECONDS);
    
    /**
     * Add a tracked race to the master that includes a competitor; check that the boat was properly replicated to
     * the replica's {@link CompetitorAndBoatStore}. Afterwards, use the {@link UpdateBoat} operation on the master to
     * perform an explicit update; ensure that the update arrived on the replica. Then execute an
     * {@link AllowBoatResetToDefaults} operation on the master, afterwards update the boat on the master,
     * @throws URISyntaxException 
     */
    @Test
    public void testPostOperationToMaster() throws InterruptedException, URISyntaxException {
        final String leaderboardName = "My Leaderboard";
        final CreateFlexibleLeaderboard operation = new CreateFlexibleLeaderboard(/* leaderboardName */ leaderboardName,
                /* leaderboardDisplayName */ null, /* discardThresholds */ new int[0], /* scoringScheme */ new LowPoint(),
                /* courseAreaId */ UUID.randomUUID());
        replica.apply(operation);
        Thread.sleep(1000);
        assertTrue(master.getLeaderboards().containsKey(leaderboardName));
        final Leaderboard masterLeaderboard = master.getLeaderboards().get(leaderboardName);
        assertTrue(masterLeaderboard.getScoringScheme() instanceof LowPoint);
    }
}