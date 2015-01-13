package com.sap.sailing.server.gateway.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.impl.LeaderboardJsonGetServlet;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardJsonExportTest extends AbstractJsonExportTest {
    private Regatta regatta;
    private RegattaLeaderboard regattaLeaderboard;
    private String boatClassName = "49er";
    private String regattaBaseName = "TestRegatta";
    
    @Before
    public void setUp() {
        super.setUp();
    }
    
    private void dropAndCreateRegatta() {
        service.getDB().dropDatabase();
        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        List<String> raceColumnNames = new ArrayList<String>();
        fleets.add(new FleetImpl("Fleet1"));
        fleets.add(new FleetImpl("Fleet2"));
        Series testSeries = new SeriesImpl("TestSeries", /* isMedal */false, fleets,
                raceColumnNames, /* trackedRegattaRegistry */null);
        series.add(testSeries);
        regatta = racingEventService.createRegatta(RegattaImpl.getDefaultName(regattaBaseName, boatClassName), boatClassName, 
                /*startDate*/ null, /*endDate*/ null, UUID.randomUUID(), series, /*persistent*/ true,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /* useStartTimeInference */ true);
        testSeries.addRaceColumn("R1", /* trackedRegattaRegistry */ null);
        testSeries.addRaceColumn("R2", /* trackedRegattaRegistry */ null);
        List<Competitor> competitors = createCompetitors(4);
        List<Competitor> fleet1Competitors = competitors.subList(0, 2);
        List<Competitor> fleet2Competitors = competitors.subList(2, 4);
        
        TimePoint now = MillisecondsTimePoint.now();
        
        RaceColumn r1Column = series.get(0).getRaceColumnByName("R1");
        TrackedRace r1Fleet1 = new MockedTrackedRaceWithStartTimeAndRanks(now, fleet1Competitors);
        TrackedRace r1Fleet2 = new MockedTrackedRaceWithStartTimeAndRanks(now, fleet2Competitors);
        r1Column.setTrackedRace(r1Column.getFleetByName("Fleet1"), r1Fleet1);
        r1Column.setTrackedRace(r1Column.getFleetByName("Fleet2"), r1Fleet2);

        regattaLeaderboard = racingEventService.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "Testregatta displayName", new int[] { 3, 5 });
        
    }
    
    @Test
    public void testExportEmptyLeaderboardAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Final.name());      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(LeaderboardJsonGetServlet.ResultStates.Final.name().equals(jsonResultState));

        // resultTimepoint should be null if there are no 'final' results yet 
        TimePoint resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        assertNull(resultTimePoint);
        
        JSONArray jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertTrue(jsonCompetitors.size() == 4);
    }
    
    @Test
    public void testExportLeaderboardWithFinalResultStateAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Final.name());      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(LeaderboardJsonGetServlet.ResultStates.Final.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        assertNull(resultTimePoint);
        
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());    
        jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;

        resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        assertNotNull(resultTimePoint);
     }

    @Test
    public void testExportLeaderboardWithLiveResultStateAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Live.name());      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(LeaderboardJsonGetServlet.ResultStates.Live.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        assertNotNull(resultTimePoint);
        
        JSONArray jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertTrue(jsonCompetitors.size() == 4);
    }

    @Test
    public void testBypassSmartFutureCacheInExportLeaderboardAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Live.name());
        requestParameters.put("useCache", "false");

        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());    

        Thread.sleep(100);

        // call the servlet for the first time
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(LeaderboardJsonGetServlet.ResultStates.Live.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        TimePoint requestTimepoint = parseTimepointFromJsonString((String) jsonObject.get("requestTimepoint"));
        assertNotNull(resultTimePoint);

        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());    

        Thread.sleep(100);
        
        // second call - it's expected NOT to get the same result as we bypass the cache
        String jsonString2 = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj2= JSONValue.parse(jsonString2);
        JSONObject jsonObject2 = (JSONObject) obj2;
        
        String jsonLeaderboardName2 = (String) jsonObject2.get("name");
        String jsonResultState2 = (String) jsonObject2.get("resultState");
        
        assertTrue(jsonLeaderboardName.equals(jsonLeaderboardName2));
        assertTrue(jsonResultState.equals(jsonResultState2));

        TimePoint resultTimePoint2 = parseTimepointFromJsonString((String) jsonObject2.get("resultTimepoint"));
        TimePoint requestTimepoint2 = parseTimepointFromJsonString((String) jsonObject2.get("requestTimepoint"));
        assertNotNull(resultTimePoint2);
        System.out.println("resultTimePoint: " + resultTimePoint.toString());
        System.out.println("resultTimePoint2: " + resultTimePoint2.toString());
        assertFalse(resultTimePoint.equals(resultTimePoint2));
        assertFalse(requestTimepoint.equals(requestTimepoint2));        
    }
    
    @Test
    public void testSmartFutureCacheInExportLeaderboardAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Final.name());

        TimePoint now = MillisecondsTimePoint.now();
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(now);    

        Thread.sleep(100);

        // call the servlet for the first time
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(LeaderboardJsonGetServlet.ResultStates.Final.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        TimePoint requestTimepoint = parseTimepointFromJsonString((String) jsonObject.get("requestTimepoint"));
        assertNotNull(resultTimePoint);
        
        // second call - it's expected to get the same result from the cache
        Thread.sleep(100);

        String jsonString2 = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj2= JSONValue.parse(jsonString2);
        JSONObject jsonObject2 = (JSONObject) obj2;
        
        String jsonLeaderboardName2 = (String) jsonObject2.get("name");
        String jsonResultState2 = (String) jsonObject2.get("resultState");
        
        assertTrue(jsonLeaderboardName.equals(jsonLeaderboardName2));
        assertTrue(jsonResultState.equals(jsonResultState2));

        TimePoint resultTimePoint2 = parseTimepointFromJsonString((String) jsonObject2.get("resultTimepoint"));
        TimePoint requestTimepoint2 = parseTimepointFromJsonString((String) jsonObject2.get("requestTimepoint"));
        assertNotNull(resultTimePoint2);
        assertTrue(resultTimePoint.equals(resultTimePoint2));
        System.out.println("requestTimepoint: " + requestTimepoint.toString());
        System.out.println("requestTimepoint2: " + requestTimepoint2.toString());
        assertFalse(requestTimepoint.equals(requestTimepoint2));
    }

    @Test
    public void testExportLeaderboardWithMaxCompetitorsCountAsJson() throws Exception {
        dropAndCreateRegatta();
        Integer maxCompetitorsCount = 2;
        
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("leaderboardName", regatta.getName());      
        requestParameters.put("resultState", LeaderboardJsonGetServlet.ResultStates.Final.name());      
        
        String jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(LeaderboardJsonGetServlet.ResultStates.Final.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonString((String) jsonObject.get("resultTimepoint"));
        assertNull(resultTimePoint);
        
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());    

        // first call for 'all' competitors
        requestParameters.put("maxCompetitorsCount", null);      
        jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;
        JSONArray jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertEquals(jsonCompetitors.size(), 4);

        Thread.sleep(100);

        // second call with maxCompetitorsCount set
        requestParameters.put("maxCompetitorsCount", maxCompetitorsCount.toString());      
        jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;
        jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertEquals((Integer) jsonCompetitors.size(), maxCompetitorsCount);
        
        Thread.sleep(100);

        // third call for 'all' competitors
        requestParameters.put("maxCompetitorsCount", null);      
        jsonString = callJsonHttpServlet(new LeaderboardJsonGetServlet(), "GET", requestParameters);
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;
        jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertEquals(jsonCompetitors.size(), 4);
     }
}
