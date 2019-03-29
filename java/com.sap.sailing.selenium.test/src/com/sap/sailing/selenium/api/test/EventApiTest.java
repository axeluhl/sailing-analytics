package com.sap.sailing.selenium.api.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.AbstractApiTest;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;

public class EventApiTest extends AbstractApiTest {

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void createAndGetEventTest() {
        ApiContext ctx = ApiContext.createApiContext(getContextRoot(), SERVER_CONTEXT, "admin", "admin");

        String eventName = "<ppp> loggingsession";
        EventApi eventApi = new EventApi();

        JSONObject createdEvent = eventApi.createEvent(ctx, eventName, "75QMNATIONALEKREUZER", "CLOSED", "default");
        assertNotNull("create: event.eventid is missing", createdEvent.get("eventid"));
        assertNotNull("create: event.eventstartdate is missing", createdEvent.get("eventstartdate"));
        assertNotNull("create: event.eventenddate is missing", createdEvent.get("eventenddate"));
        assertEquals("create: event.eventname is different", eventName, createdEvent.get("eventname"));
        assertEquals("create: event.reagattaname is different", eventName, createdEvent.get("regatta"));
        assertEquals("create: event.leaderboard is different", eventName, createdEvent.get("leaderboard"));

        JSONObject foundEvent = eventApi.getEvent(ctx, (String) createdEvent.get("eventid"));
        assertNotNull("read: event.id is missing", foundEvent.get("id"));
        assertEquals("read: event.name is different", eventName, foundEvent.get("name"));
        assertEquals("read: event.description is different", eventName, foundEvent.get("description"));
        assertEquals("read: event.officialWebsiteURL is different", null, foundEvent.get("officialWebsiteURL"));
        assertEquals("read: event.baseUrl is different", getContextRoot() + SERVER_CONTEXT + "/api/",
                foundEvent.get("baseURL"));
        assertNotNull("read: event.startDate is missing", foundEvent.get("startDate"));
        assertNotNull("read: event.endDate is missing", foundEvent.get("endDate"));
        assertEquals("read: event.images should be empty", 0, ((JSONArray) foundEvent.get("images")).size());
        assertEquals("read: event.videos should be empty", 0, ((JSONArray) foundEvent.get("videos")).size());
        assertEquals("read: event.sailorsInfoWebsiteURLs", 0,
                ((JSONArray) foundEvent.get("sailorsInfoWebsiteURLs")).size());

        JSONObject foundEventVenue = (JSONObject) foundEvent.get("venue");
        assertEquals("read: event.venue.name is different", "default", foundEventVenue.get("name"));

        JSONArray foundEventVenueCourseAreas = (JSONArray) foundEventVenue.get("courseAreas");
        JSONObject foundEventVenueCourseAreasFirst = (JSONObject) foundEventVenueCourseAreas.get(0);
        assertEquals("read: event.venue.courseAreas[0].name is different", "Default",
                foundEventVenueCourseAreasFirst.get("name"));
        assertNotNull("read: event.venue.courseAreas[0].id is null", foundEventVenueCourseAreasFirst.get("id"));

        JSONArray foundEventLeaderBoardGroups = (JSONArray) foundEvent.get("leaderboardGroups");
        JSONObject foundEventLeaderBoardGroupsFirst = (JSONObject) foundEventLeaderBoardGroups.get(0);
        assertNotNull("read: event.leaderboardGroups[0].id is null", foundEventLeaderBoardGroupsFirst.get("id"));
        assertEquals("read: event.leaderboardGroups[0].name is different", eventName,
                foundEventLeaderBoardGroupsFirst.get("name"));
        assertEquals("read: event.leaderboardGroups[0].description is different", eventName,
                foundEventLeaderBoardGroupsFirst.get("description"));
        assertEquals("read: event.leaderboardGroups[0].hasOverallLeaderboard is different", false,
                foundEventLeaderBoardGroupsFirst.get("hasOverallLeaderboard"));
    }
}
