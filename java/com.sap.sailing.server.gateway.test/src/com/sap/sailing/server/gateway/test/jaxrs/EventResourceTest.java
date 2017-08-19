package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.server.gateway.jaxrs.api.EventsResource;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sailing.server.gateway.jaxrs.api.RegattasResource;

public class EventResourceTest extends AbstractJaxRsApiTest 

{
    @Before
    public void setUp() {
        super.setUp();
    }
    
    @Test
    public void testCreateEvent() throws Exception {         
        EventsResource resource = new EventsResource();
        EventsResource spyResource = spyResource(resource);
        String eventName = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        
        Response eventResponse = spyResource.createEvent(eventName, eventName, null, null, eventName, null, null, null, null, null, null, null);
        
        String eventId = (String) eventResponse.getEntity();
        System.out.println(eventId);
        
        assert(eventId != null);
    }
    
    @Test
    public void testCreateEventWithLeaderboardGroup() throws Exception {         
        EventsResource resource = new EventsResource();
        EventsResource spyResource = spyResource(resource);
        String eventName = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        
        Response eventResponse = spyResource.createEvent(eventName, eventName, null, null, eventName, null, null, null, null, "true", null, null);
        
        String eventId = (String) eventResponse.getEntity();
        assert(eventId != null);
        
        String jsonString = (String) spyResource.getEvent(eventId).getEntity();
        JSONObject obj= (JSONObject) JSONValue.parse(jsonString);
       
        JSONArray lgs = (JSONArray) obj.get("leaderboardGroups");
        assertTrue(lgs.size() == 1);
    }
    
    @Test
    public void testCreateEventWithLeaderboardGroupAndRegatta() throws Exception {         
        EventsResource resource = new EventsResource();
        EventsResource spyResource = spyResource(resource);
        String eventName = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        
        String eventId = (String) spyResource.createEvent(eventName, eventName, null, null, eventName, null, null, null, null, "true", "true", "A_CAT").getEntity();
        assert(eventId != null);
        
        String jsonEvent = (String) spyResource.getEvent(eventId).getEntity();
        JSONObject event= (JSONObject) JSONValue.parse(jsonEvent);
       
        JSONArray lgs = (JSONArray) event.get("leaderboardGroups");
        assertTrue(lgs.size() == 1);
        
        JSONObject venue = (JSONObject) event.get("venue");
        JSONArray courseAreas = (JSONArray) venue.get("courseAreas");
        assertTrue(courseAreas.size() == 1);
        
        JSONObject courseArea = (JSONObject) courseAreas.get(0);
        String courseAreaId = (String) courseArea.get("id");
        System.out.println(courseAreaId);
        assert(courseAreaId != null);
        
        RegattasResource regattaResource = new RegattasResource();
        RegattasResource spyRegattaResource = spyResource(regattaResource);
        
        Response regattasResponse = spyRegattaResource.getRegatta(eventName);
        String jsonRegatta = (String) regattasResponse.getEntity();
        JSONObject regatta = (JSONObject) JSONValue.parse(jsonRegatta);
        String regattaName = (String) regatta.get("name");
        
        String regattaCourseAreaId = (String) regatta.get("courseAreaId");
        assertTrue(regattaCourseAreaId.equals(courseAreaId));
        
        LeaderboardsResource leaderboardResource = spyResource(new LeaderboardsResource());
        Response leaderboardResponse = leaderboardResource.getLeaderboard(regattaName, LeaderboardsResource.ResultStates.Final, null);
        String jsonLeaderboard = (String) leaderboardResponse.getEntity();
        JSONObject leaderboard = (JSONObject) JSONValue.parse(jsonRegatta);
        String leaderboardName = (String)leaderboard.get("name");
        assertTrue(leaderboardName.equals(regattaName));
        
    }

}
