package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.jms.JMSException;

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
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.test.MockedTrackedRaceWithFixedRank;
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
        assertNull(replica.getLeaderboardByName(leaderboardName));
        final int[] discardThresholds = new int[] { 19, 44 };
        CreateFlexibleLeaderboard createTestLeaderboard = new CreateFlexibleLeaderboard(leaderboardName, discardThresholds);
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
        master = new RacingEventServiceImpl(MongoDBService.INSTANCE);
        final Leaderboard masterLeaderboardReloaded = master.getLeaderboardByName(leaderboardName);
        assertNotNull(masterLeaderboardReloaded);
        // expecting the correction to only be in the DelayedLeaderboardCorrection object but not the leaderboard itself
        assertEquals(MaxPointsReason.NONE, masterLeaderboardReloaded.getMaxPointsReason(hasso,
                masterLeaderboardReloaded.getRaceColumnByName(Q2), MillisecondsTimePoint.now()));
        
        replicaReplicator.startToReplicateFrom(masterDescriptor);
        
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
