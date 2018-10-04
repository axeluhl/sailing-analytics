package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.server.gateway.jaxrs.api.RegattasResource;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattasResourceTest extends AbstractJaxRsApiTest {
    private String boatClassName = "49er";
    private String regattaNamePart = "TestRegatta";
    private String regattaName = RegattaImpl.getDefaultName(regattaNamePart, boatClassName);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<Series> series = new ArrayList<Series>();
        List<Fleet> fleets = new ArrayList<Fleet>();
        List<String> raceColumnNames = new ArrayList<String>();
        fleets.add(new FleetImpl("Fleet1"));
        fleets.add(new FleetImpl("Fleet2"));
        final Calendar cal = new GregorianCalendar();
        cal.set(2014, 5, 6, 10, 00);
        final TimePoint startDate = new MillisecondsTimePoint(cal.getTime());
        cal.set(2014, 5, 8, 16, 00);
        final TimePoint endDate = new MillisecondsTimePoint(cal.getTime());
        Series testSeries = new SeriesImpl("TestSeries", /* isMedal */false, /* isFleetsCanRunInParallel */ true, fleets, raceColumnNames, /* trackedRegattaRegistry */null);
        series.add(testSeries);
        racingEventService.createRegatta(regattaName, boatClassName, 
                /* canBoatsOfCompetitorsChangePerRace */ true, startDate, endDate, UUID.randomUUID(), series, /*persistent*/ true,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        testSeries.addRaceColumn("R1", /* trackedRegattaRegistry */ null);
        testSeries.addRaceColumn("R2", /* trackedRegattaRegistry */ null);


        Course course = new CourseImpl("emptyCourse", Collections.emptySet());
        // get the same instance! of the boat class object, as else addRace will fail
        BoatClass boatClass = racingEventService.getBaseDomainFactory().getOrCreateBoatClass(boatClassName);
        racingEventService.addRace(new RegattaName(regattaName), new RaceDefinitionImpl("Race 1", course, boatClass));
    }

    @Test
    public void testGetRegattas() throws Exception {         
        RegattasResource resource = new RegattasResource();
        RegattasResource spyResource = spyResource(resource);
        
        Response regattasResponse = spyResource.getRegattas();
        
        String jsonString = (String) regattasResponse.getEntity();
        Object obj= JSONValue.parse(jsonString);
        JSONArray array= (JSONArray) obj;

        assertTrue(array.size() == 1);

        JSONObject firstElement = (JSONObject) array.get(0);  
        String jsonName = (String) firstElement.get("name");
        String jsonBoatClass = (String) firstElement.get("boatclass");
        
        assertTrue(RegattaImpl.getDefaultName(regattaNamePart, boatClassName).equals(jsonName));
        assertTrue(boatClassName.equals(jsonBoatClass));
    }

    @Test
    public void testNullCheckForTrackedRaceInGetManeuvers() throws Exception {
        RegattasResource resource = new RegattasResource();
        RegattasResource spyResource = spyResource(resource);

        Response response = spyResource.getManeuvers(regattaName, "Race 1", null,
                null);
        // the current race is not tracked, expect an error
        assertTrue(response.getStatus() != 200);
    }

}
