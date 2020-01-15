package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.domain.common.CompetitorRegistrationType.CLOSED;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi.DeviceMappingRequest;
import com.sap.sailing.selenium.api.event.LeaderboardApi.Leaderboard;
import com.sap.sailing.selenium.api.event.LeaderboardApi.TrackingTimes;
import com.sap.sailing.selenium.api.regatta.Competitor;
import com.sap.sailing.selenium.api.regatta.RaceColumn;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class LeaderboardApiTest extends AbstractSeleniumTest {

    private static final String BOATCLASSNAME = "75QMNATIONALEKREUZER";
    private static final String LEADERBOARD_NAME = "loggingsession";

    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi leaderboardApi = new LeaderboardApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void testGetLeaderboardForCreatedEvent() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        eventApi.createEvent(ctx, LEADERBOARD_NAME, BOATCLASSNAME, CLOSED, "default");
        Leaderboard leaderBoard = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertEquals("read: leaderboard.name is different", LEADERBOARD_NAME, leaderBoard.getName());
        assertEquals("read: leaderboard.displayName is different", LEADERBOARD_NAME, leaderBoard.getDisplayName());
        assertNotNull("read: leaderboard.resultTimepoint is missing", leaderBoard.getResultTimePoint());
        assertEquals("read: leaderboard.resultState is different", "Live", leaderBoard.getResultState());
        assertEquals("read: leaderboard.type is different", "RegattaLeaderboard", leaderBoard.getType());
        assertEquals("read: leaderboard.canBoatsOfCompetitorsChangePerRace is different", false,
                leaderBoard.canBoatsOfCompetitorsChangePerRace());
        assertNull("read: leaderboard.maxCompetitorsCount should be null", leaderBoard.getMaxCompetitorsCount());
        assertNull("read: leaderboard.scoringComment should be null", leaderBoard.getScoringComment());
        assertNull("read: leaderboard.lastScoringUpdate should be null", leaderBoard.getLastScoringUpdate());
        assertEquals("read: leaderboard.columnNames should be empty", 0, leaderBoard.getColumnNames().length);
        assertEquals("read: leaderboard.competitors should be empty", 0,
                ((JSONArray) leaderBoard.get("competitors")).size());
        assertEquals("read: leaderboard.ShardingLeaderboardName is different",
                "/leaderboard/" + LEADERBOARD_NAME.replaceAll(" ", "_").replaceAll("<", "_").replaceAll(">", "_"),
                leaderBoard.get("ShardingLeaderboardName"));
    }

    @Test
    public void testUpdatingLeaderboard() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        eventApi.createEvent(ctx, LEADERBOARD_NAME, BOATCLASSNAME, CLOSED, "default");
        final Leaderboard leaderboardOriginal = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);

        // updating
        final int[] newResultDiscardingThresholds = new int[] { 1, 2, 3, 4, 5 };
        leaderboardApi.updateLeaderboard(ctx, LEADERBOARD_NAME, /* leaderboardDisplayName */ null,
                newResultDiscardingThresholds);

        // reloading and check if updated
        Leaderboard leaderboardReloaded = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertArrayEquals(newResultDiscardingThresholds,
                leaderboardReloaded.getDiscardIndexResultsStartingWithHowManyRaces());

        // remove and check again
        final int[] newEmptyResultDiscardingThresholds = new int[] {};
        leaderboardApi.updateLeaderboard(ctx, LEADERBOARD_NAME, /* leaderboardDisplayName */ null,
                newEmptyResultDiscardingThresholds);
        leaderboardReloaded = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertArrayEquals(newEmptyResultDiscardingThresholds,
                leaderboardReloaded.getDiscardIndexResultsStartingWithHowManyRaces());

        // passing null should not change anything
        leaderboardApi.updateLeaderboard(ctx, LEADERBOARD_NAME, /* leaderboardDisplayName */ null,
                newResultDiscardingThresholds);
        leaderboardReloaded = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertArrayEquals(newResultDiscardingThresholds,
                leaderboardReloaded.getDiscardIndexResultsStartingWithHowManyRaces());
        leaderboardApi.updateLeaderboard(ctx, LEADERBOARD_NAME, /* leaderboardDisplayName */ null, null);
        assertArrayEquals(newResultDiscardingThresholds,
                leaderboardReloaded.getDiscardIndexResultsStartingWithHowManyRaces());

        // check that no other atributes have been changed
        assertEquals(leaderboardOriginal.getName(), leaderboardReloaded.getName());
        assertEquals(leaderboardOriginal.getDisplayName(), leaderboardReloaded.getDisplayName());
        
        // update leaderboardDisplayName
        leaderboardApi.updateLeaderboard(ctx, LEADERBOARD_NAME, "another display name", null);
        leaderboardReloaded = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertEquals("another display name", leaderboardReloaded.getDisplayName());
        
        // check that no other atributes have been changed
        assertEquals(leaderboardOriginal.getName(), leaderboardReloaded.getName());
        assertArrayEquals(newResultDiscardingThresholds,
                leaderboardReloaded.getDiscardIndexResultsStartingWithHowManyRaces());
        
        // update leaderboardDisplayName and resultDiscardingThresholds
        final int[] newResultDiscardingThresholds2 = new int[] {7, 8};
        leaderboardApi.updateLeaderboard(ctx, LEADERBOARD_NAME, "another display name 2", newResultDiscardingThresholds2);
        leaderboardReloaded = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertEquals("another display name 2", leaderboardReloaded.getDisplayName());
        assertArrayEquals(newResultDiscardingThresholds2, newResultDiscardingThresholds2);
    }

    @Test
    public void testDeviceMappingStartAndEnd() throws Exception {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        eventApi.createEvent(ctx, LEADERBOARD_NAME, BOATCLASSNAME, CLOSED, "default");
        final Competitor competitor = regattaApi.createAndAddCompetitor(ctx, LEADERBOARD_NAME, BOATCLASSNAME, "test@de",
                "Max Mustermann", "USA");
        final DeviceMappingRequest request = leaderboardApi.createDeviceMappingRequest(ctx, LEADERBOARD_NAME)
                .forCompetitor(competitor.getId()).withDeviceUuid(randomUUID());
        @SuppressWarnings("unused") // TODO: check result
        JSONObject mappingStart = request.startDeviceMapping(currentTimeMillis());
        @SuppressWarnings("unused") // TODO: check result
        JSONObject mappingEnd = request.endDeviceMapping(currentTimeMillis());
    }

    @Test
    public void testSetTrackingTimes() throws Exception {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        eventApi.createEvent(ctx, LEADERBOARD_NAME, BOATCLASSNAME, CLOSED, "default");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, LEADERBOARD_NAME, null, 1)[0];
        final Long startTime = currentTimeMillis();
        final Long endTime = currentTimeMillis() + 10000L;
        leaderboardApi.startRaceLogTracking(ctx, LEADERBOARD_NAME, race.getRaceName(), "Default");
        TrackingTimes trackingTimes = leaderboardApi.setTrackingTimes(ctx, LEADERBOARD_NAME, race.getRaceName(),
                "Default", startTime, endTime);
        assertEquals("read: startTime is different", startTime, trackingTimes.getStartOfTracking());
        assertEquals("read: endTime is different", endTime, trackingTimes.getEndOfTracking());
    }
}
