package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.api.EventsResource;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardGroupsResource;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sailing.server.gateway.jaxrs.api.RegattasResource;

public class EventResourceTest extends AbstractJaxRsApiTest 

{
    private EventsResource eventsResource;
    private RegattasResource regattasResource;
    private LeaderboardGroupsResource leaderboardGroupsResource;
    private LeaderboardsResource leaderboardsResource;
    private String randomName; 
    
    @Before
    public void setUp() {
        super.setUp();
        eventsResource = createResource(new EventsResource());
        regattasResource = createResource(new RegattasResource());
        leaderboardGroupsResource = createResource(new LeaderboardGroupsResource());
        leaderboardsResource = createResource(new LeaderboardsResource());
        randomName = randomName();
    }
    
    @Test
    public void testCreateEvent() throws Exception {         
        Response eventResponse = createEvent();
        assertTrue(isValidEventResponse(eventResponse));
    }
    
    @Test
    public void testCreateEventWithLeaderboardGroup() throws Exception {         
        Response eventResponse = createEventWithLeaderboardGroup();
        assertTrue(isValidEventResponse(eventResponse));
        
        JSONObject objEvent = getEvent(getIdFromResponse(eventResponse));
        assertTrue(hasDefaultLeaderboardGroup(objEvent));
    }

    @Test
    public void testCreateEventWithLeaderboardGroupAndRegatta() throws Exception {         

        Response eventResponse = createEventWithLeaderboardGroupAndRegatta();
        assert(isValidEventResponse(eventResponse));
        
        JSONObject objEvent = getEvent(getIdFromResponse(eventResponse));
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
    public void testCreateEventAddLeaderboardGroup() throws Exception {         
        String eventName = randomName();
        
        Response eventResponse = createEvent();
        String strEventId = getIdFromResponse(eventResponse);
        
        Response addLeaderboardGroupResponse = addLeaderboardGroup(eventName, strEventId);
        String strLeaderboardGroupId = getIdFromResponse(addLeaderboardGroupResponse);
        assertTrue(isValidLeaderboardGroupResponse(addLeaderboardGroupResponse));
        
        JSONObject objEvent = getEvent(strEventId);
        JSONObject objLeaderboardGroup = (JSONObject) getLeaderboardGroups(objEvent).get(0);
        String strEventLeaderboardGroupId = (String) objLeaderboardGroup.get("id");
        
        assertTrue(strEventLeaderboardGroupId.equals(strLeaderboardGroupId));
    }

    @Test
    public void testCreateEventAddRegatta() throws Exception {         
        String eventName = randomName();
        
        Response eventResponse = eventsResource.createEvent(eventName, eventName, null, null, eventName, null, null, null, null, null, null, null);
        assertTrue(isValidEventResponse(eventResponse));
        
        String strEventId = getIdFromResponse(eventResponse);
        Response regattaResponse = createRegatta(eventName, strEventId);
        assertTrue(isValidRegattaResponse(regattaResponse));
        
        JSONObject regatta = getRegatta(eventName);
        String strRegattaCourseAreaId = (String) regatta.get("courseAreaId");
        
        JSONArray arrCourseAreas = getCourseAreasOfEvent(strEventId);
        assertTrue(arrCourseAreas.size() == 1);
        
        JSONObject objCourseArea = (JSONObject) arrCourseAreas.get(0);
        String strCourseAreaId = (String) objCourseArea.get("id");
        
        assertTrue(strCourseAreaId.equals(strRegattaCourseAreaId)); 
        
        String strRegattaName = (String) regatta.get("name");
        assertTrue(leaderBoardWithNameExists(strRegattaName));
    }
    
    @Test
    public void testCreateEventWithLeaderboardGroupAddRegatta() throws Exception {   
        String eventName = randomName;
        Response eventResponse = createEventWithLeaderboardGroup();
        assertTrue(isValidEventResponse(eventResponse));
        
        String strEventId = getIdFromResponse(eventResponse);
        Response regattaResponse = createRegatta(eventName, strEventId);
        assertTrue(isValidRegattaResponse(regattaResponse));
        
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

    private Response createEventWithLeaderboardGroup()
            throws MalformedURLException, ParseException, NotFoundException {
        return eventsResource.createEvent(randomName, randomName, null, null, randomName, null, null, null, null, "true", null, null);
    }

    private Response createEvent() throws MalformedURLException, ParseException, NotFoundException {
        return eventsResource.createEvent(randomName, randomName, null, null, randomName, null, null, null, null, null, null, null);
    }
    
    private Response createEventWithLeaderboardGroupAndRegatta()
            throws MalformedURLException, ParseException, NotFoundException {
        return eventsResource.createEvent(randomName, randomName, null, null, randomName, null, null, null, null, "true", "true", "A_CAT");
    }
    
    private Response createRegatta(String eventName, String strEventId) throws ParseException, NotFoundException {
        return eventsResource.addRegatta(eventName, "A_CAT", null, strEventId, null,null, null, null, null, null, null,null);
    }

    private Response addLeaderboardGroup(String leaderboardGroupName, String strEventId) throws NotFoundException {
        return eventsResource.addLeaderboardGroup(strEventId, leaderboardGroupName, leaderboardGroupName, null, null, null, null, null);
    }
    
    private Response getLeaderboard(String name) {
        return leaderboardsResource.getLeaderboard(name, LeaderboardsResource.ResultStates.Final, null);
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
        return toJSONObject(getIdFromResponse(regattasResponse));
    }

    private boolean isValidEventResponse(Response response) {
        String id = getIdFromResponse(response);
        return validateUUID(id);
    }
    
    private boolean isValidLeaderboardGroupResponse(Response response) {
        String id = getIdFromResponse(response);
        return validateUUID(id);
    }
    
    private boolean isValidRegattaResponse(Response response) {
        String id = getIdFromResponse(response);
        return validateUUID(id);
    }

    private JSONArray getCourseAreasOfEvent(String strEventId) {
        JSONObject objEvent = getEvent(strEventId);
        
        JSONArray arrCourseAreas = getCourseAreas(objEvent);
        return arrCourseAreas;
    }

    private JSONObject getEvent(String strEventId) {
        String jsonEvent = getEventAsString(strEventId);
        JSONObject objEvent= toJSONObject(jsonEvent);
        return objEvent;
    }

    private boolean leaderBoardWithNameExists(String name) {
        Response leaderboardResponse = getLeaderboard(name);
        JSONObject objLeaderboard = getEntityAsObject(leaderboardResponse);
        String strLeaderboardName = (String) objLeaderboard.get("name");
        return strLeaderboardName.equals(name);
    }

    private JSONObject getEntityAsObject(Response leaderboardGroupsResponse) {
        String strLeaderboardGroup = getIdFromResponse(leaderboardGroupsResponse);
        JSONObject objLeaderboardGroup = toJSONObject(strLeaderboardGroup);
        return objLeaderboardGroup;
    }

    private JSONObject getLeaderboardGroup(String strDefaultLeaderboardGroupName) {
        Response leaderboardGroupsResponse = leaderboardGroupsResource.getLeaderboardGroup(strDefaultLeaderboardGroupName);
        return toJSONObject(getIdFromResponse(leaderboardGroupsResponse));
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
    

    private String getIdFromResponse(Response eventResponse) {
        return (String) eventResponse.getEntity();
    }

    private <T extends AbstractSailingServerResource> T createResource(T resource){
        return spyResource(resource);
    }
    
    private String getEventAsString(String eventId) {
        return getIdFromResponse(eventsResource.getEvent(eventId));
    }

}
