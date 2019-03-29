package com.sap.sailing.selenium.api.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.AbstractApiTest;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.RegattaApi;

public class RegattaApiTest extends AbstractApiTest {

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetRegattaForCreatedEvent() {
        ApiContext ctx = ApiContext.createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");

        String eventName = "<ppp> loggingsession";
        String boatClass = "75QMNATIONALEKREUZER";
        String competitorRegistrationType = "CLOSED";
        EventApi eventApi = new EventApi();
        RegattaApi regattaApi = new RegattaApi();

        eventApi.createEvent(ctx, eventName, boatClass, competitorRegistrationType, "default");
        JSONObject regatta = regattaApi.getRegatta(ctx, eventName);
        JSONArray series = (JSONArray) regatta.get("series");
        JSONObject serie = (JSONObject) series.get(0);
        JSONArray fleets = (JSONArray) serie.get("fleets");
        JSONObject trackedRaces = (JSONObject) serie.get("trackedRaces");

        assertEquals("read: regatta.name is different", eventName, regatta.get("name"));
        assertNull("read: regatta.startDate should be null", regatta.get("startDate"));
        assertNull("read: regatta.endDate should be null", regatta.get("endDate"));
        assertEquals("read: regatta.scoringSystem is different", "LOW_POINT", regatta.get("scoringSystem"));
        assertEquals("read: regatta.boeatclass is different", boatClass, regatta.get("boatclass"));
        assertNotNull("read: regatta.courseAreaId is missing", regatta.get("courseAreaId"));
        assertEquals("read: regatta.canBoatsOfCompetitorsChangePerRace should be false", false,
                regatta.get("canBoatsOfCompetitorsChangePerRace"));
        assertEquals("read: regatta.competitorRegistrationType is different", competitorRegistrationType,
                regatta.get("competitorRegistrationType"));

        assertEquals("read: reagtta.series should have 1 entry", 1, series.size());
        assertEquals("read: reagtta.fleets should have 1 entry", 1, fleets.size());
        assertNotNull("read: reagtta.trackedRaces is missing", trackedRaces);
    }

    @Test
    public void testGetRacesForRegattaForCreateEvent() {
        ApiContext ctx = ApiContext.createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");
        EventApi eventApi = new EventApi();
        RegattaApi regattaApi = new RegattaApi();
        String eventName = "<ppp> loggingsession";

        eventApi.createEvent(ctx, eventName, "75QMNATIONALEKREUZER", "CLOSED", "default");
        JSONObject regattaRaces = regattaApi.getRegattaRaces(ctx, eventName);
        JSONArray races = (JSONArray) regattaRaces.get("races");

        assertEquals("read: regatta is different", eventName, regattaRaces.get("regatta"));
        assertEquals("read: reagtta.series should have 0 entries", 0, races.size());
    }

    @Test
    public void testCreateAndAddCompetitor() {
        ApiContext ctx = ApiContext.createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");
        EventApi eventApi = new EventApi();
        RegattaApi regattaApi = new RegattaApi();
        String eventName = "<ppp> loggingsession";
        String boatClassName = "75QMNATIONALEKREUZER";

        eventApi.createEvent(ctx, eventName, boatClassName, "CLOSED", "default");
        JSONObject competitor = regattaApi.createAndAddCompetitor(ctx, eventName, boatClassName, "test@de",
                "Max Mustermann", "USA");
        assertNotNull("competitor.id is missing", competitor.get("id"));
    }
}
