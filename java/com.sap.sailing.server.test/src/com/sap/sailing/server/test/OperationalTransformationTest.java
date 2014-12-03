package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnUp;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboard;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.operationaltransformation.OperationalTransformer;
import com.sap.sse.operationaltransformation.Peer;
import com.sap.sse.operationaltransformation.PeerImpl;
import com.sap.sse.operationaltransformation.Peer.Role;

public class OperationalTransformationTest {
    private static final String LEADERBOARDNAME = "TESTBOARD";

    private RacingEventService racingEventServiceServer;
    private RacingEventService racingEventServiceReplica;
    private Peer<RacingEventServiceOperation<?>, RacingEventService> server;
    private Peer<RacingEventServiceOperation<?>, RacingEventService> replica;

    @Before
    public void setUp() {
        // ensure that leaderboards will be loaded from and stored to the test database which is 
        MongoDBService.INSTANCE.getDB().dropDatabase();
        racingEventServiceServer = new RacingEventServiceImpl();
        racingEventServiceReplica = new RacingEventServiceImpl();
        OperationalTransformer<RacingEventService, RacingEventServiceOperation<?>> transformer = new OperationalTransformer<>();
        server = new PeerImpl<>(transformer, racingEventServiceServer, Role.SERVER);
        replica = new PeerImpl<>(transformer, racingEventServiceReplica, Role.CLIENT);
        // wire the peers:
        server.addPeer(replica);
        replica.addPeer(server);
    }

    @Test
    public void testAddLeaderboard() {
        RacingEventServiceOperation<FlexibleLeaderboard> addLeaderboardOp = new CreateFlexibleLeaderboard(LEADERBOARDNAME,null, new int[] { 5 },
                new LowPoint(), null);
        server.apply(addLeaderboardOp);
        server.waitForNotRunning();
        replica.waitForNotRunning();
        Map<String, Leaderboard> replicaLeaderboards = racingEventServiceReplica.getLeaderboards();
        assertEquals(2, replicaLeaderboards.size()); // expected to include the default leaderboard
        assertEquals(new HashSet<String>(Arrays.asList(new String[] { LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, LEADERBOARDNAME })),
                replicaLeaderboards.keySet());
        assertEquals(racingEventServiceServer.getLeaderboards().keySet(), replicaLeaderboards.keySet());
    }

    @Test
    public void testAddColumnToLeaderboardOnServerAndRemoveLeaderboardOnClient() throws InterruptedException {
        RacingEventServiceOperation<RaceColumn> addLeaderboardColumn = new AddColumnToLeaderboard(
                "newColumn", LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, /* medalRace */ true);
        server.apply(addLeaderboardColumn);
        RacingEventServiceOperation<Void> removeDefaultLeaderboard = new RemoveLeaderboard(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME);
        replica.apply(removeDefaultLeaderboard);
        replica.waitForNotRunning();
        server.waitForNotRunning();
        assertEquals(0, racingEventServiceReplica.getLeaderboards().size());
        assertEquals(0, racingEventServiceServer.getLeaderboards().size());
    }

    @Test
    public void testAddOneColumnOnEachSideThenMoveOneUpOnServerAndRemoveLeaderboardOnClient() throws InterruptedException {
        RacingEventServiceOperation<RaceColumn> addLeaderboardColumnOnServer = new AddColumnToLeaderboard(
                "newColumn1", LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, /* medalRace */ true);
        server.apply(addLeaderboardColumnOnServer);
        RacingEventServiceOperation<RaceColumn> addLeaderboardColumnOnReplica = new AddColumnToLeaderboard(
                "newColumn2", LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, /* medalRace */ true);
        replica.apply(addLeaderboardColumnOnReplica);
        replica.waitForNotRunning();
        server.waitForNotRunning();
        RacingEventServiceOperation<Void> moveUpNewColumn2 = new MoveLeaderboardColumnUp(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME, "newColumn2");
        server.apply(moveUpNewColumn2);
        RacingEventServiceOperation<Void> removeDefaultLeaderboard = new RemoveLeaderboard(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME);
        replica.apply(removeDefaultLeaderboard);
        replica.waitForNotRunning();
        server.waitForNotRunning();
        assertEquals(0, racingEventServiceReplica.getLeaderboards().size());
        assertEquals(0, racingEventServiceServer.getLeaderboards().size());
    }
}
