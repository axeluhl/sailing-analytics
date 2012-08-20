package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithFixedRank;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardMaxPointsReason;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

public class DelayedLeaderboardCorrectionsReplicationTest extends AbstractServerReplicationTest {
    private static final String Q2 = "Q2";
    private ReplicationService replicaReplicator;
    private ReplicationMasterDescriptor masterDescriptor;
    
    @Before
    public void setUp() throws FileNotFoundException, UnknownHostException {
        final MongoDBService mongoDBService = MongoDBService.INSTANCE;
        mongoDBService.getDB().dropDatabase();
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
    public void testDelayedLeaderboardCorrectionsReplication() throws Exception {
        // prepare a leaderboard with corrections on master by creating through RacingEventService which stores it; then
        // create a new RacingEventServiceImpl which is expected to load the leaderboard, but only with DelayedLeaderboardCorrections.
        // then start initial load, wait until finished; then link a tracked race to leaderboard's RaceColumn on master and
        // verify that on the replica it is associated to the RaceColumn as well, and the DelayedLeaderboardCorrections are
        // resolved properly on the replica.
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        Competitor hasso = AbstractLeaderboardTest.createCompetitor("Dr. Hasso Plattner");
        final TrackedRace q2YellowTrackedRace = new MockedTrackedRaceWithFixedRank(hasso, /* rank */ 1, /* started */ false, boatClass) {
            private static final long serialVersionUID = 1234L;
            @Override
            public RegattaAndRaceIdentifier getRaceIdentifier() {
                return new RegattaNameAndRaceName("Kieler Woche (5o5)", "Yellow Race 2");
            }
        };
        master = createRacingEventServiceWithOneMockedTrackedRace(q2YellowTrackedRace);
        final String leaderboardName = "My new leaderboard";
        final int[] discardThresholds = new int[] { 19, 44 };
        CreateFlexibleLeaderboard createTestLeaderboard = new CreateFlexibleLeaderboard(leaderboardName, discardThresholds, new LowPoint());
        assertNull(master.getLeaderboardByName(leaderboardName));
        master.apply(createTestLeaderboard);
        final Leaderboard masterLeaderboard = master.getLeaderboardByName(leaderboardName);
        assertNotNull(masterLeaderboard);
        master.apply(new AddColumnToLeaderboard(Q2, leaderboardName, /* medalRace */ false)); // uses leaderboard's default fleet 
        master.apply(new ConnectTrackedRaceToLeaderboardColumn(masterLeaderboard.getName(), Q2, /* default fleet */
                masterLeaderboard.getFleet(null).getName(), q2YellowTrackedRace.getRaceIdentifier()));
        master.apply(new UpdateLeaderboardMaxPointsReason(masterLeaderboard.getName(), Q2, hasso.getId().toString(),
                MaxPointsReason.DNF, MillisecondsTimePoint.now()));
        assertEquals(MaxPointsReason.DNF, masterLeaderboard.getMaxPointsReason(hasso,
                masterLeaderboard.getRaceColumnByName(Q2), MillisecondsTimePoint.now()));

        // re-load new master from persistence
        master = createRacingEventServiceWithOneMockedTrackedRace(q2YellowTrackedRace);
        replica = createRacingEventServiceWithOneMockedTrackedRace(q2YellowTrackedRace);
        final Leaderboard masterLeaderboardReloaded = master.getLeaderboardByName(leaderboardName);
        assertNotNull(masterLeaderboardReloaded);
        // expecting the correction to only be in the DelayedLeaderboardCorrection object but not the leaderboard itself
        assertEquals(MaxPointsReason.NONE, masterLeaderboardReloaded.getMaxPointsReason(hasso,
                masterLeaderboardReloaded.getRaceColumnByName(Q2), MillisecondsTimePoint.now()));

        // replicate the re-loaded environment
        Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> descriptors = basicSetUp(/* dropDB */ false, master, replica);
        replicaReplicator = descriptors.getA();
        masterDescriptor = descriptors.getB();
        replicaReplicator.startToReplicateFrom(masterDescriptor);
        Thread.sleep(1000);

        Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboardName);
        assertNotNull(replicaLeaderboard);
        assertNotNull(replicaLeaderboard.getRaceColumnByName(Q2));
        // so far, the replica should also only have the delayed corrections:
        assertEquals(MaxPointsReason.NONE, replicaLeaderboard.getMaxPointsReason(hasso,
                replicaLeaderboard.getRaceColumnByName(Q2), MillisecondsTimePoint.now()));
        
        // now connect the tracked race again to the leaderboard column in the re-loaded environment
        master.apply(new ConnectTrackedRaceToLeaderboardColumn(masterLeaderboard.getName(), Q2, /* default fleet */
                masterLeaderboard.getFleet(null).getName(), q2YellowTrackedRace.getRaceIdentifier()));
        // now the delayed corrections are expected to have been resolved:
        assertEquals(MaxPointsReason.DNF, master.getLeaderboardByName(leaderboardName).getMaxPointsReason(hasso,
                master.getLeaderboardByName(leaderboardName).getRaceColumnByName(Q2), MillisecondsTimePoint.now()));
        Thread.sleep(1000); // wait for the tracked race to column connection to be replicated
        assertNotNull(replicaLeaderboard.getRaceColumnByName(Q2).getTrackedRace(hasso));
        assertEquals(MaxPointsReason.DNF, replicaLeaderboard.getMaxPointsReason(hasso,
                replicaLeaderboard.getRaceColumnByName(Q2), MillisecondsTimePoint.now()));
    }
    
    private RacingEventServiceImpl createRacingEventServiceWithOneMockedTrackedRace(final TrackedRace q2YellowTrackedRace) {
        return new RacingEventServiceImpl(MongoDBService.INSTANCE) {
            @Override
            public TrackedRace getExistingTrackedRace(RaceIdentifier raceIdentifier) {
                return q2YellowTrackedRace;
            }
        };
    }
}
