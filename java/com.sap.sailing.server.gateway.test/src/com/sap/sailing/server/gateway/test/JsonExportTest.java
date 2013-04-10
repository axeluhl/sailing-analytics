package com.sap.sailing.server.gateway.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.impl.LeaderboardGroupsJsonGetServlet;
import com.sap.sailing.server.gateway.impl.LeaderboardJsonGetServlet;
import com.sap.sailing.server.gateway.impl.RegattasJsonGetServlet;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class JsonExportTest {
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
        
        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        List<String> raceColumnNames = new ArrayList<String>();

        fleets.add(new FleetImpl("Fleet1"));
        fleets.add(new FleetImpl("Fleet2"));

        raceColumnNames.add("R1");
        raceColumnNames.add("R2");
        
        Series testSeries = new SeriesImpl("TestSeries", /* isMedal */false, fleets,
                raceColumnNames, /* trackedRegattaRegistry */null);
        series.add(testSeries);

        regatta = racingEventService.createRegatta(regattaName, boatClassName, UUID.randomUUID(), series, /*persistent*/ true,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null);

        List<Competitor> competitors = createCompetitors(4);
        List<Competitor> fleet1Competitors = competitors.subList(0, 1);
        List<Competitor> fleet2Competitors = competitors.subList(2, 3);
        
        TimePoint now = MillisecondsTimePoint.now();
        
        RaceColumn r1Column = series.get(0).getRaceColumnByName("R1");
        TrackedRace r1Fleet1 = new MockedTrackedRaceWithStartTimeAndRanks(now, fleet1Competitors);
        TrackedRace r1Fleet2 = new MockedTrackedRaceWithStartTimeAndRanks(now, fleet2Competitors);
        r1Column.setTrackedRace(r1Column.getFleetByName("Fleet1"), r1Fleet1);
        r1Column.setTrackedRace(r1Column.getFleetByName("Fleet2"), r1Fleet2);

        regattaLeaderboard = racingEventService.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "Testregatta displayName", new int[] { 3, 5 });

        List<String> leaderboardNames = new ArrayList<String>();
        leaderboardNames.add(regatta.getName());
        leaderboardGroup = racingEventService.addLeaderboardGroup(leaderboardGroupName, "description", /* displayGroupsInReverseOrder */ false, 
                leaderboardNames, /* overallLeaderboardDiscardThresholds */ null, /* overallLeaderboardScoringSchemeType */ null);
    }

    @Test
    public void testExportRegattasAsJson() throws Exception {          
        String jsonString = callJsonHttpServlet(new RegattasJsonGetServlet(), "GET", null);
        
        Object obj= JSONValue.parse(jsonString);
        JSONArray array= (JSONArray) obj;

        assertTrue(array.size() == 1);

        JSONObject firstElement = (JSONObject) array.get(0);  
        String jsonName = (String) firstElement.get("name");
        String jsonBoatClass = (String) firstElement.get("boatclass");
        
        assertTrue(RegattaImpl.getDefaultName(regattaName, boatClassName).equals(jsonName));
        assertTrue(boatClassName.equals(jsonBoatClass));
    }

    @Test
    public void testExportLeaderboardGroupsAsJson() throws Exception {   
        String jsonString = callJsonHttpServlet(new LeaderboardGroupsJsonGetServlet(), "GET", null);
        
        Object obj= JSONValue.parse(jsonString);
        JSONArray array= (JSONArray) obj;
        
        assertTrue(array.size() == 1);

        String jsonFirstLeaderboardGroup = (String) array.get(0);

        assertTrue(leaderboardGroup.getName().equals(jsonFirstLeaderboardGroup));
    }

    @Test
    public void testExportLeaderboardWithFinalResultStateAsJson() throws Exception {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Final.name());      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(jsonResultState.equals(LeaderboardJsonGetServlet.ResultStates.Final.name()));

        String jsonResultTimePoint = (String) jsonObject.get("resultTimepoint");
        assertNull(jsonResultTimePoint);
        
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());    
        jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;

        jsonResultTimePoint = (String) jsonObject.get("resultTimepoint");
        assertNotNull(jsonResultTimePoint);
     }

    @Test
    public void testExportLeaderboardWithLiveResultStateAsJson() throws Exception {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Live.name());      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(jsonResultState.equals(LeaderboardJsonGetServlet.ResultStates.Live.name()));

        String jsonResultTimePoint = (String) jsonObject.get("resultTimepoint");
        assertNotNull(jsonResultTimePoint);
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

    private List<Competitor> createCompetitors(int numberOfCompetitorsToCreate) {
        List<Competitor> result = new ArrayList<Competitor>();
        BoatClass boatClass = new BoatClassImpl("505", /* typicallyStartsUpwind */ true);
        for (int i = 1; i <= numberOfCompetitorsToCreate; i++) {
            String competitorName = "C" + i;
            Competitor competitor = new CompetitorImpl(123, competitorName, new TeamImpl("STG", Collections.singleton(
                    new PersonImpl(competitorName, new NationalityImpl("GER"),
                            /* dateOfBirth */ null, "This is famous "+competitorName)),
                            new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                            /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                                    boatClass, null)); 
            result.add(competitor);
        }
        return result;
    }
}
