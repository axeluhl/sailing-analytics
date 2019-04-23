package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.domain.common.CompetitorRegistrationType.CLOSED;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
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
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.RegattaApi.Competitor;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class LeaderboardApiTest extends AbstractSeleniumTest {

    private static final String BOATCLASSNAME = "75QMNATIONALEKREUZER";
    private static final String LEADERBOARD_NAME = "loggingsession";

    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi leaderboardApi = new LeaderboardApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetLeaderboardForCreatedEvent() {
        final ApiContext ctx = createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");

        eventApi.createEvent(ctx, LEADERBOARD_NAME, BOATCLASSNAME, CLOSED, "default");

        JSONObject leaderBoard = leaderboardApi.getLeaderboard(ctx, LEADERBOARD_NAME);
        assertEquals("read: leaderboard.name is different", LEADERBOARD_NAME, leaderBoard.get("name"));
        assertEquals("read: leaderboard.displayName is different", LEADERBOARD_NAME, leaderBoard.get("displayName"));
        assertNotNull("read: leaderboard.resultTimepoint is missing", leaderBoard.get("resultTimepoint"));
        assertEquals("read: leaderboard.resultState is different", "Live", leaderBoard.get("resultState"));
        assertEquals("read: leaderboard.type is different", "RegattaLeaderboard", leaderBoard.get("type"));
        assertEquals("read: leaderboard.canBoatsOfCompetitorsChangePerRace is different", false,
                leaderBoard.get("canBoatsOfCompetitorsChangePerRace"));
        assertNull("read: leaderboard.maxCompetitorsCount should be null", leaderBoard.get("maxCompetitorsCount"));
        assertNull("read: leaderboard.scoringComment should be null", leaderBoard.get("scoringComment"));
        assertNull("read: leaderboard.lastScoringUpdate should be null", leaderBoard.get("lastScoringUpdate"));
        assertEquals("read: leaderboard.columnNames should be empty", 0,
                ((JSONArray) leaderBoard.get("columnNames")).size());
        assertEquals("read: leaderboard.competitors should be empty", 0,
                ((JSONArray) leaderBoard.get("competitors")).size());
        assertEquals("read: leaderboard.ShardingLeaderboardName is different",
                "/leaderboard/" + LEADERBOARD_NAME.replaceAll(" ", "_").replaceAll("<", "_").replaceAll(">", "_"),
                leaderBoard.get("ShardingLeaderboardName"));
    }

    @Test
    public void testDeviceMappingStartAndEnd() throws Exception {
        final ApiContext ctx = createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");

        eventApi.createEvent(ctx, LEADERBOARD_NAME, BOATCLASSNAME, CLOSED, "default");
        final Competitor competitor = regattaApi.createAndAddCompetitor(ctx, LEADERBOARD_NAME, BOATCLASSNAME,
                "test@de", "Max Mustermann", "USA");

        final DeviceMappingRequest request = leaderboardApi.createDeviceMappingRequest(ctx, LEADERBOARD_NAME)
                .forCompetitor(competitor.getId()).withDeviceUuid(randomUUID());

        @SuppressWarnings("unused") // TODO: check result
        JSONObject mappingStart = request.startDeviceMapping(currentTimeMillis());
        @SuppressWarnings("unused") // TODO: check result
        JSONObject mappingEnd = request.endDeviceMapping(currentTimeMillis());
    }
}
