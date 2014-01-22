package com.sap.sailing.server.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class LeaderboardStorageTest extends TestCase {

    private static final String LEADERBOARD_NAME = "test";

    @Override
    protected void setUp() throws Exception {
        removeTestLeaderboard();
    }

    @Override
    protected void tearDown() throws Exception {
        removeTestLeaderboard();
    }

    private void removeTestLeaderboard() {
        RacingEventService service = new RacingEventServiceImpl();
        if (service.getLeaderboardByName(LEADERBOARD_NAME) != null) {
            service.removeLeaderboard(LEADERBOARD_NAME);
        }
    }

    @Test
    public void testIfCarriedPointsAreStoredIfNoRacesAreTracked() {
        RacingEventService service = new RacingEventServiceImpl();
        int[] dicardingThresholds = {};
        Leaderboard leaderboard = service.addFlexibleLeaderboard(LEADERBOARD_NAME, "testIt", dicardingThresholds,
                new LowPoint(), "maaap");

        List<DynamicPerson> sailorList = new ArrayList<DynamicPerson>();
        sailorList.add(new PersonImpl("sailor", new NationalityImpl("GER"), null, ""));
        DynamicTeam team = new TeamImpl("team", sailorList, null);
        DynamicBoat boat = new BoatImpl("woot", service.getBaseDomainFactory().getOrCreateBoatClass("H16"), "70155");
        String competitorId = "testC";
        Competitor competitor = service.getBaseDomainFactory().getOrCreateCompetitor(competitorId, "Test C", null,
                team, boat);

        double carriedPoints = 2.0;
        leaderboard.setCarriedPoints(competitor, carriedPoints);

        RacingEventService restartedService = new RacingEventServiceImpl();
        leaderboard = restartedService.getLeaderboardByName(LEADERBOARD_NAME);
        competitor = restartedService.getBaseDomainFactory().getExistingCompetitorById(competitorId);
        assertEquals(carriedPoints, leaderboard.getCarriedPoints(competitor), 0.0001);
    }

}
