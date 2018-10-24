package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

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
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.jaxrs.api.AbstractLeaderboardsResource;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardsResourceTest extends AbstractJaxRsApiTest {
    private Regatta regatta;
    private RegattaLeaderboard regattaLeaderboard;
    private String boatClassName = "49er";
    private String regattaBaseName = "TestRegatta";
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    private void dropAndCreateRegatta() {
        service.getDB().dropDatabase();
        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        List<String> raceColumnNames = new ArrayList<String>();
        fleets.add(new FleetImpl("Fleet1"));
        fleets.add(new FleetImpl("Fleet2"));
        Series testSeries = new SeriesImpl("TestSeries", /* isMedal */false, /* isFleetsCanRunInParallel */ true, fleets,
                raceColumnNames, /* trackedRegattaRegistry */null);
        series.add(testSeries);
        regatta = racingEventService.createRegatta(RegattaImpl.getDefaultName(regattaBaseName, boatClassName), boatClassName, 
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED,
                /* registrationLinkSecret */ null, /* startDate */ null, /* endDate */ null, UUID.randomUUID(), series,
                /* persistent */ true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null,
                /* buoyZoneRadiusInHullLengths */2.0, /* useStartTimeInference */ true,
                /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        testSeries.addRaceColumn("R1", /* trackedRegattaRegistry */ null);
        testSeries.addRaceColumn("R2", /* trackedRegattaRegistry */ null);
        List<Competitor> competitors = createCompetitors(4);
        List<Competitor> fleet1Competitors = competitors.subList(0, 2);
        List<Competitor> fleet2Competitors = competitors.subList(2, 4);
        
        TimePoint now = MillisecondsTimePoint.now();
        
        RaceColumn r1Column = series.get(0).getRaceColumnByName("R1");
        TrackedRace r1Fleet1 = new MockedTrackedRaceWithStartTimeAndRanks(now, fleet1Competitors, regatta);
        TrackedRace r1Fleet2 = new MockedTrackedRaceWithStartTimeAndRanks(now, fleet2Competitors, regatta);
        r1Column.setTrackedRace(r1Column.getFleetByName("Fleet1"), r1Fleet1);
        r1Column.setTrackedRace(r1Column.getFleetByName("Fleet2"), r1Fleet2);

        regattaLeaderboard = racingEventService.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "Testregatta displayName", new int[] { 3, 5 });
        
    }
    
    @Test
    public void testExportEmptyLeaderboardAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Response leaderboardReponse = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        String jsonString = (String) leaderboardReponse.getEntity();
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(AbstractLeaderboardsResource.ResultStates.Final.name().equals(jsonResultState));

        // resultTimepoint should be null if there are no 'final' results yet 
        TimePoint resultTimePoint = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNull(resultTimePoint);
        
        JSONArray jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertTrue(jsonCompetitors.size() == 4);
    }
    
    @Test
    public void testExportLeaderboardWithFinalResultStateAsJson() throws Exception {
        dropAndCreateRegatta();
        
        Response leaderboardReponse = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        String jsonString = (String) leaderboardReponse.getEntity();
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertEquals(regattaLeaderboard.getName(), jsonLeaderboardName);
        assertEquals(AbstractLeaderboardsResource.ResultStates.Final.name(), jsonResultState);

        TimePoint resultTimePoint = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNull(resultTimePoint);
        
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());
        
        Response leaderboardReponse2 = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        String jsonString2 = (String) leaderboardReponse2.getEntity();        
        obj= JSONValue.parse(jsonString2);
        jsonObject = (JSONObject) obj;

        resultTimePoint = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNotNull(resultTimePoint);
     }

    @Test
    public void testExportLeaderboardWithLiveResultStateAsJson() throws Exception {
        dropAndCreateRegatta();
        Response leaderboardReponse = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Live, null);
        String jsonString = (String) leaderboardReponse.getEntity();
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(AbstractLeaderboardsResource.ResultStates.Live.name().equals(jsonResultState));
        TimePoint resultTimePoint = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNotNull(resultTimePoint);
        JSONArray jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertTrue(jsonCompetitors.size() == 4);
    }
    
    @Test
    public void testSmartFutureCacheInExportLeaderboardAsJson() throws Exception {
        dropAndCreateRegatta();
        
        TimePoint now = MillisecondsTimePoint.now();
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(now);    

        Thread.sleep(100);

        // call the servlet for the first time
        Response leaderboardReponse = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        String jsonString = (String) leaderboardReponse.getEntity();
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(AbstractLeaderboardsResource.ResultStates.Final.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNotNull(resultTimePoint);
        
        // second call - it's expected to get the same result from the cache
        Thread.sleep(100);

        Response leaderboardReponse2 = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        String jsonString2 = (String) leaderboardReponse2.getEntity();
        Object obj2= JSONValue.parse(jsonString2);
        JSONObject jsonObject2 = (JSONObject) obj2;
        
        String jsonLeaderboardName2 = (String) jsonObject2.get("name");
        String jsonResultState2 = (String) jsonObject2.get("resultState");
        
        assertTrue(jsonLeaderboardName.equals(jsonLeaderboardName2));
        assertTrue(jsonResultState.equals(jsonResultState2));

        TimePoint resultTimePoint2 = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNotNull(resultTimePoint2);
        assertTrue(resultTimePoint.equals(resultTimePoint2));
    }

    @Test
    public void testExportLeaderboardWithMaxCompetitorsCountAsJson() throws Exception {
        dropAndCreateRegatta();
        Integer maxCompetitorsCount = 2;
        
        Response leaderboardReponse = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        String jsonString = (String) leaderboardReponse.getEntity();
        Object obj= JSONValue.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        
        String jsonLeaderboardName = (String) jsonObject.get("name");
        String jsonResultState = (String) jsonObject.get("resultState");
        
        assertTrue(regattaLeaderboard.getName().equals(jsonLeaderboardName));
        assertTrue(AbstractLeaderboardsResource.ResultStates.Final.name().equals(jsonResultState));

        TimePoint resultTimePoint = parseTimepointFromJsonNumber((Long) jsonObject.get("resultTimepoint"));
        assertNull(resultTimePoint);
        
        regattaLeaderboard.getScoreCorrection().setTimePointOfLastCorrectionsValidity(MillisecondsTimePoint.now());    

        // first call for 'all' competitors
        Response leaderboardReponse2 = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        jsonString = (String) leaderboardReponse2.getEntity();
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;
        JSONArray jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertEquals(jsonCompetitors.size(), 4);

        Thread.sleep(100);

        // second call with maxCompetitorsCount set
        Response leaderboardReponse3 = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, maxCompetitorsCount);

        jsonString = (String) leaderboardReponse3.getEntity();
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;
        jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertEquals((Integer) jsonCompetitors.size(), maxCompetitorsCount);
        
        Thread.sleep(100);

        // third call for 'all' competitors
        Response leaderboardReponse4 = leaderboardsResource.getLeaderboard(regatta.getName(),
                AbstractLeaderboardsResource.ResultStates.Final, null);

        jsonString = (String) leaderboardReponse4.getEntity();
        obj= JSONValue.parse(jsonString);
        jsonObject = (JSONObject) obj;
        jsonCompetitors = (JSONArray) jsonObject.get("competitors");
        assertEquals(jsonCompetitors.size(), 4);
     }
}
