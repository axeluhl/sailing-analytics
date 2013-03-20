package com.sap.sailing.server.gateway.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.impl.LeaderboardGroupsJsonExportServlet;
import com.sap.sailing.server.gateway.impl.LeaderboardJsonExportServlet;
import com.sap.sailing.server.gateway.impl.RegattasJsonExportServlet;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class JsonExportTests {
    private RacingEventService racingEventService;
    private Regatta regatta;
    private RegattaLeaderboard regattaLeaderboard;
    private LeaderboardGroup leaderboardGroup;
    private String boatClassName = "49er";
    private String regattaName = "TestRegatta";
    private String leaderboardGroupName = "TestLeaderboardGroup";

    private MongoDBService service;
    
    @Before
    public void setUp() {
        service = MongoDBService.INSTANCE;
        service.setConfiguration(MongoDBConfiguration.getDefaultTestConfiguration());
        service.getDB().dropDatabase();
        
        racingEventService = new RacingEventServiceImpl();
        
        Serializable id = new RegattaName(regattaName);
        regatta = racingEventService.getOrCreateRegatta(regattaName, boatClassName, id);
        
        regattaLeaderboard = racingEventService.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "Testregatta displayName", new int[] { 3, 5 });

        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(regatta.getName());
        leaderboardGroup = racingEventService.addLeaderboardGroup(leaderboardGroupName, "description", /* displayGroupsInReverseOrder */ false, 
                leaderboardNames, /* overallLeaderboardDiscardThresholds */ null, /* overallLeaderboardScoringSchemeType */ null);
    }

    @Test
    public void createAndexportRegattasAsJsonTest() throws Exception {          
        String jsonString = callJsonHttpServlet(new RegattasJsonExportServlet(), "GET", null);
        
        Object obj= JSONValue.parse(jsonString);
        JSONArray array= (JSONArray) obj;

        assertTrue(array.size() == 1);

        JSONObject firstElement = (JSONObject) array.get(0);  
        String jsonName = (String) firstElement.get("name");
        String jsonBoatClass = (String) firstElement.get("boatclass");
        
        assertTrue(RegattaImpl.getFullName(regattaName, boatClassName).equals(jsonName));
        assertTrue(boatClassName.equals(jsonBoatClass));
    }

    @Test
    public void exportLeaderboardGroupsAsJsonTest() throws Exception {   
        String jsonString = callJsonHttpServlet(new LeaderboardGroupsJsonExportServlet(), "GET", null);
        
        Object obj= JSONValue.parse(jsonString);
        JSONArray array= (JSONArray) obj;
        
        assertTrue(array.size() == 1);

        String jsonFirstLeaderboardGroup = (String) array.get(0);

        assertTrue(leaderboardGroup.getName().equals(jsonFirstLeaderboardGroup));
    }

    @Test
    public void exportLeaderboardAsJsonTest() throws Exception {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", "Final");      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonExportServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
     }

    private String callJsonHttpServlet(AbstractJsonHttpServlet jsonServlet, String GetOrPostMethod, Map<String, String> parameters) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);    
        when(request.getMethod()).thenReturn(GetOrPostMethod);          

        if(parameters != null) {
            for(Map.Entry<String, String> entry: parameters.entrySet()) {
                when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
            }
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        AbstractJsonHttpServlet spyJsonExportServlet = spy(jsonServlet);
        doReturn(racingEventService).when(spyJsonExportServlet).getService();
        
        spyJsonExportServlet.service(request, response);

        // the writer may not have been flushed yet...
        writer.flush(); 
        
        return stringWriter.toString();
    }

}
