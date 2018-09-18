package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.api.AbstractLeaderboardsResource;
import com.sap.sailing.server.gateway.jaxrs.api.EventsResource;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardGroupsResource;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sailing.server.gateway.jaxrs.api.RegattasResource;
import com.sap.sse.InvalidDateException;

public class EventResourceTest extends AbstractJaxRsApiTest {
    private EventsResource eventsResource;
    private RegattasResource regattasResource;
    private LeaderboardGroupsResource leaderboardGroupsResource;
    private LeaderboardsResource leaderboardsResource;
    private String randomName; 
    private UriInfo uriInfo;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(new URI("http://127.0.0.1:8888/"));
        eventsResource = createResource(new EventsResource(/* enforce security */ false));
        regattasResource = createResource(new RegattasResource());
        leaderboardGroupsResource = createResource(new LeaderboardGroupsResource());
        leaderboardsResource = createResource(new LeaderboardsResource());
        randomName = randomName();
    }
    
    @Test
    public void testCreateEvent() throws Exception {
        Response eventResponse = createEvent();
        assertTrue(isValidCreateEventResponse(eventResponse));
        JSONObject objEvent = getEvent(getIdFromCreateEventResponse(eventResponse));
        assertFalse(hasDefaultLeaderboardGroup(objEvent));
    }
    
    @Test
    public void testCreateEventInBerlin() throws Exception {
        Response eventResponse = createEventAtLocation(new DegreePosition(52.514176, 13.411628));
        assertTrue(isValidCreateEventResponse(eventResponse));
        JSONObject objEvent = getEvent(getIdFromCreateEventResponse(eventResponse));
        assertFalse(hasDefaultLeaderboardGroup(objEvent));
        assertEquals("Nikolaiviertel", ((JSONObject) objEvent.get("venue")).get("name"));
    }
    
    @Test
    public void testCreateEventWithLeaderboardGroup() throws Exception {
        Response eventResponse = createEventWithLeaderboardGroup();
        assertTrue(isValidCreateEventResponse(eventResponse));
        JSONObject objEvent = getEvent(getIdFromCreateEventResponse(eventResponse));
        assertTrue(hasDefaultLeaderboardGroup(objEvent));
    }

    @Test
    public void testCreateEventWithLeaderboardGroupAndRegatta() throws Exception {
        Response eventResponse = createEventWithLeaderboardGroupAndRegatta();
        assert(isValidCreateEventResponse(eventResponse));
        JSONObject objEvent = getEvent(getIdFromCreateEventResponse(eventResponse));
        assertTrue(hasDefaultLeaderboardGroup(objEvent));
        assertTrue(hasAtLeastOneCourseArea(objEvent));
        JSONObject objRegatta = getRegatta(randomName);
        String strRegattaName = (String) objRegatta.get("name");
        String strRegattaCourseAreaId = (String) objRegatta.get("courseAreaId");
        assertTrue(strRegattaCourseAreaId.equals(getDefaultCourseAreaId(objEvent)));
        leaderBoardWithNameExists(strRegattaName);
        JSONArray leaderboardGroups = getLeaderboardGroups(objEvent);
        assertTrue(hasDefaultLeaderboardGroup(objEvent));
        JSONObject objDefaultLeaderboardGroup = (JSONObject) leaderboardGroups.get(0);
        String strDefaultLeaderboardGroupName = (String) objDefaultLeaderboardGroup.get("name");
        JSONObject objLeaderboardGroup = getLeaderboardGroup(strDefaultLeaderboardGroupName);
        JSONArray leaderboards  = (JSONArray) objLeaderboardGroup.get("leaderboards");
        assertTrue(containsObjectWithAttrbuteNameAndValue(leaderboards, "name", randomName));
    }

    @Test
    public void testCreateEventWithLeaderboardGroupAddRegatta() throws Exception {
        String eventName = randomName;
        Response eventResponse = createEventWithLeaderboardGroupAndRegatta();
        assertTrue(isValidCreateEventResponse(eventResponse));
        String strEventId = getIdFromCreateEventResponse(eventResponse);
        JSONObject regatta = getRegatta(eventName);
        String strRegattaCourseAreaId = (String) regatta.get("courseAreaId");
        JSONArray arrCourseAreas = getCourseAreasOfEvent(strEventId);
        assertTrue(arrCourseAreas.size() == 1);
        JSONObject objCourseArea = (JSONObject) arrCourseAreas.get(0);
        String strCourseAreaId = (String) objCourseArea.get("id");
        assertTrue(strCourseAreaId.equals(strRegattaCourseAreaId)); 
        String strRegattaName = (String) regatta.get("name");
        assertTrue(leaderBoardWithNameExists(strRegattaName));
        JSONObject objLeaderboardGroup = getLeaderboardGroup(eventName);
        JSONArray arrLeaderboards = (JSONArray) objLeaderboardGroup.get("leaderboards");
        assertTrue(containsObjectWithAttrbuteNameAndValue(arrLeaderboards, "name", strRegattaName));
    }

    private Response createEventWithLeaderboardGroup() throws ParseException, NotFoundException, NumberFormatException,
            IOException, org.json.simple.parser.ParseException, InvalidDateException {
        return eventsResource.createEvent(uriInfo, randomName, randomName, /* startDateParam */ null,
                /* startDateAsMillis */ null, /* endDateParam */ null, /* endDateAsMillis */ null,
                /* venueNameParam */ randomName, /* venueLat */ null, /* venueLng */ null, /* isPublicParam */ null,
                /* officialWebsiteURLParam */ null, /* baseURLParam */ null, /* leaderboardGroupIdsListParam */ null,
                /* createLeaderboardGroupParam */ "true", /* createRegattaParam */ "false",
                /* boatClassNameParam */ null, /* numberOfRacesParam */ null, false, /* canCompetitorsRegisterToOpenRegatta */ false);
    }

    private Response createEvent() throws ParseException, NotFoundException, NumberFormatException, IOException,
            org.json.simple.parser.ParseException, InvalidDateException {
        return eventsResource.createEvent(uriInfo, randomName, randomName, /* startDateParam */ null,
                /* startDateAsMillis */ null, /* endDateParam */ null, /* endDateAsMillis */ null,
                /* venueNameParam */ randomName, /* venueLat */ null, /* venueLng */ null, /* isPublicParam */ null,
                /* officialWebsiteURLParam */ null, /* baseURLParam */ null, /* leaderboardGroupIdsListParam */ null,
                /* createLeaderboardGroupParam */ "false", /* createRegattaParam */ "false",
                /* boatClassNameParam */ null, /* numberOfRacesParam */ null, false, /* canCompetitorsRegisterToOpenRegatta */ false);
    }

    private Response createEventAtLocation(Position location) throws ParseException, NotFoundException,
            NumberFormatException, IOException, org.json.simple.parser.ParseException, InvalidDateException {
        return eventsResource.createEvent(uriInfo, randomName, randomName, /* startDateParam */ null,
                /* startDateAsMillis */ null, /* endDateParam */ null, /* endDateAsMillis */ null,
                /* venueNameParam */ null, /* venueLat */ "" + location.getLatDeg(),
                /* venueLng */ "" + location.getLngDeg(), /* isPublicParam */ null, /* officialWebsiteURLParam */ null,
                /* baseURLParam */ null, /* leaderboardGroupIdsListParam */ null,
                /* createLeaderboardGroupParam */ "false", /* createRegattaParam */ "false",
                /* boatClassNameParam */ null, /* numberOfRacesParam */ null, false, /* canCompetitorsRegisterToOpenRegatta */ false);
    }

    private Response createEventWithLeaderboardGroupAndRegatta() throws ParseException, NotFoundException,
            NumberFormatException, IOException, org.json.simple.parser.ParseException, InvalidDateException {
        return eventsResource.createEvent(uriInfo, randomName, randomName, /* startDateParam */ null,
                /* startDateAsMillis */ null, /* endDateParam */ null, /* endDateAsMillis */ null,
                /* venueNameParam */ randomName, /* venueLat */ null, /* venueLng */ null, /* isPublicParam */ null,
                /* officialWebsiteURLParam */ null, /* baseURLParam */ null, /* leaderboardGroupIdsListParam */ null,
                /* createLeaderboardGroupParam */ "true", /* createRegattaParam */ "true",
                /* boatClassNameParam */ "A_CAT", /* numberOfRacesParam */ null, false, /* canCompetitorsRegisterToOpenRegatta */ false);
    }
    
    private Response getLeaderboard(String name) {
        return leaderboardsResource.getLeaderboard(name, AbstractLeaderboardsResource.ResultStates.Final, null);
    }
    
    private boolean hasAtLeastOneCourseArea(JSONObject objEvent) {
        String strCourseAreaId = getDefaultCourseAreaId(objEvent);
        return validateUUID(strCourseAreaId);
    }

    private String getDefaultCourseAreaId(JSONObject objEvent) {
        JSONArray arrCourseAreas = getCourseAreas(objEvent);
        assertTrue(arrCourseAreas.size() == 1);
        
        JSONObject courseArea = (JSONObject) arrCourseAreas.get(0);
        String strCourseAreaId = (String) courseArea.get("id");
        return strCourseAreaId;
    }

    private boolean hasDefaultLeaderboardGroup(JSONObject objEvent) {
        String eventName = (String) objEvent.get("name");
        return containsObjectWithAttrbuteNameAndValue(getLeaderboardGroups(objEvent), "name", eventName);
    }
    
    private boolean containsObjectWithAttrbuteNameAndValue(JSONArray array, String attributeName, String value){
        return array.stream().filter(o -> ((JSONObject) o).get(attributeName).equals(value)).findFirst().isPresent();
    }

    private JSONObject getRegatta(String eventName) {
        Response regattasResponse = regattasResource.getRegatta(eventName);
        return toJSONObject((String) regattasResponse.getEntity());
    }

    private boolean isValidCreateEventResponse(Response response) {
        String id = getIdFromCreateEventResponse(response);
        return validateUUID(id);
    }
    
    private JSONArray getCourseAreasOfEvent(String strEventId) {
        JSONObject objEvent = getEvent(strEventId);
        JSONArray arrCourseAreas = getCourseAreas(objEvent);
        return arrCourseAreas;
    }

    private JSONObject getEvent(String strEventId) {
        String jsonEvent = getEventAsString(strEventId);
        JSONObject objEvent = toJSONObject(jsonEvent);
        return objEvent;
    }

    private boolean leaderBoardWithNameExists(String name) {
        Response leaderboardResponse = getLeaderboard(name);
        JSONObject objLeaderboard = getLeaderboardAsJsonObject(leaderboardResponse);
        String strLeaderboardName = (String) objLeaderboard.get("name");
        return strLeaderboardName.equals(name);
    }

    private JSONObject getLeaderboardAsJsonObject(Response leaderboardResponse) {
        String strLeaderboardGroup = (String) leaderboardResponse.getEntity();
        JSONObject objLeaderboardGroup = toJSONObject(strLeaderboardGroup);
        return objLeaderboardGroup;
    }

    private JSONObject getLeaderboardGroup(String strDefaultLeaderboardGroupName) {
        Response leaderboardGroupsResponse = leaderboardGroupsResource.getLeaderboardGroup(strDefaultLeaderboardGroupName);
        return toJSONObject(leaderboardGroupsResponse.getEntity().toString());
    }

    private JSONArray getLeaderboardGroups(JSONObject objEvent) {
        JSONArray arrLgs = (JSONArray) objEvent.get("leaderboardGroups");
        return arrLgs;
    }

    private JSONArray getCourseAreas(JSONObject objEvent) {
        JSONObject objVenue = (JSONObject) objEvent.get("venue");
        JSONArray arrCourseAreas = (JSONArray) objVenue.get("courseAreas");
        return arrCourseAreas;
    }

    private JSONObject toJSONObject(String strEvent) {
        return (JSONObject) JSONValue.parse(strEvent);
    }

    private boolean validateUUID(String eventId) {
        return UUID.fromString(eventId) != null;
    }

    private String randomName() {
        return RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }
    

    private String getIdFromCreateEventResponse(Response createEventResponse) {
        return (String) toJSONObject((String) createEventResponse.getEntity()).get("eventid");
    }

    private <T extends AbstractSailingServerResource> T createResource(T resource){
        return spyResource(resource);
    }
    
    private String getEventAsString(String eventId) {
        return (String) eventsResource.getEvent(eventId).getEntity();
    }

}
