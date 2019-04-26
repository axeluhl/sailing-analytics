package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.domain.common.CompetitorRegistrationType.CLOSED;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
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
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.RegattaApi.Competitor;
import com.sap.sailing.selenium.api.event.RegattaApi.RaceColumn;
import com.sap.sailing.selenium.api.event.RegattaApi.Regatta;
import com.sap.sailing.selenium.api.event.RegattaApi.RegattaRaces;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class RegattaApiTest extends AbstractSeleniumTest {

    private static String EVENT_NAME = "<ppp> loggingsession";
    private static String BOAT_CLASS = "75QMNATIONALEKREUZER";

    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi leaderboardApi = new LeaderboardApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetRegattaForCreatedEvent() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CLOSED, "default");
        Regatta regatta = regattaApi.getRegatta(ctx, EVENT_NAME);
        JSONArray series = (JSONArray) regatta.get("series");
        JSONObject serie = (JSONObject) series.get(0);
        JSONArray fleets = (JSONArray) serie.get("fleets");
        JSONObject trackedRaces = (JSONObject) serie.get("trackedRaces");

        assertEquals("read: regatta.name is different", EVENT_NAME, regatta.getName());
        assertNull("read: regatta.startDate should be null", regatta.getStartDate());
        assertNull("read: regatta.endDate should be null", regatta.getEndDate());
        assertEquals("read: regatta.scoringSystem is different", "LOW_POINT", regatta.getScoringSystem());
        assertEquals("read: regatta.boeatclass is different", BOAT_CLASS, regatta.getBoatClass());
        assertNotNull("read: regatta.courseAreaId is missing", regatta.getCourseAreaId());
        assertEquals("read: regatta.canBoatsOfCompetitorsChangePerRace should be false", false,
                regatta.canBoatsOfCompetitorsChangePerRace());
        assertEquals("read: regatta.competitorRegistrationType is different", CLOSED,
                regatta.getCompetitorRegistrationType());

        assertEquals("read: reagtta.series should have 1 entry", 1, series.size());
        assertEquals("read: reagtta.fleets should have 1 entry", 1, fleets.size());
        assertNotNull("read: reagtta.trackedRaces is missing", trackedRaces);
    }

    @Test
    public void testGetRacesForRegattaForCreateEvent() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CLOSED, "default");
        RegattaRaces regattaRaces = regattaApi.getRegattaRaces(ctx, EVENT_NAME);

        assertEquals("read: regatta is different", EVENT_NAME, regattaRaces.getRegattaName());
        //assertEquals("read: reagtta.series should have 0 entries", 0, regattaRaces.getRaces().length);
        
        RaceColumn[] raceColumns = regattaApi.addRaceColumn(ctx, EVENT_NAME, "R", 1);
        leaderboardApi.startRaceLogTracking(ctx, EVENT_NAME, raceColumns[0].getRaceName(), "Default");
        regattaRaces = regattaApi.getRegattaRaces(ctx, EVENT_NAME);
        assertEquals("read: reagtta.series should have 0 entries", 1, regattaRaces.getRaces().length);
    }

    @Test
    public void testCreateAndAddCompetitor() {
        final String competitorName = "Max Mustermann";
        final String competitorNationality = "USA";
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CLOSED, "default");
        Competitor competitor = regattaApi.createAndAddCompetitor(ctx, EVENT_NAME, BOAT_CLASS, "test@de",
                competitorName, competitorNationality);
        assertNotNull("read: competitor.id is missing", competitor.getId());
        assertEquals("read: competitor.name is different", competitorName, competitor.getName());
        assertEquals("read: competitor.shortName is different", competitorName, competitor.getShortName());
        assertEquals("read: competitor.nationality is different", competitorNationality, competitor.getNationality());
        assertNotNull("read: competitor.team should not be emtpy", competitor.getTeam());
        assertNotNull("read: competitor.boat should not be empty", competitor.getBoat());
        assertNotNull("read: competitor.boat.boatClass should not be empty", competitor.getBoat().getBoatClass());
        assertEquals("read: competitor.boat.boatClass.name is differnet", BOAT_CLASS,
                competitor.getBoat().getBoatClass().getName());
    }

    @Test
    public void testAddRaceColumns() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, EVENT_NAME, BOAT_CLASS, CLOSED, "default");
        RaceColumn[] result = regattaApi.addRaceColumn(ctx, EVENT_NAME, "T", 5);
        assertEquals("read: racecolumn.seriesname is different", "Default", result[0].getSeriesName());
        assertEquals("read: racecolumn.racename is different", "T1", result[0].getRaceName());
        // assertEquals("read: racecolumn.seriesname is different", "Default", result[4].getSeriesName());
        // assertEquals("read: racecolumn.racename is different", "T5", result[4].getRaceName());
    }
}
