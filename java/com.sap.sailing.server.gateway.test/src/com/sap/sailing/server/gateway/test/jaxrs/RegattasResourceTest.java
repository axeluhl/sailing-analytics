package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.shiro.util.ThreadState;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.server.gateway.jaxrs.api.RegattasResource;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.Account;

public class RegattasResourceTest extends AbstractJaxRsApiTest {
    private String boatClassName = "49er";
    private String closedRegattaName = "TestRegatta";
    private String openRegattaName = "TestOpenRegatta";

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
        racingEventService.createRegatta(RegattaImpl.getDefaultName(closedRegattaName, boatClassName), boatClassName, 
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, startDate, endDate, UUID.randomUUID(), series, /*persistent*/ true,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        racingEventService.createRegatta(RegattaImpl.getDefaultName(openRegattaName, boatClassName), boatClassName, 
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.OPEN_MODERATED, startDate, endDate, UUID.randomUUID(), series, /*persistent*/ true,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */ true, /* controlTrackingFromStartAndFinishTimes */ false, OneDesignRankingMetric::new);
        testSeries.addRaceColumn("R1", /* trackedRegattaRegistry */ null);
        testSeries.addRaceColumn("R2", /* trackedRegattaRegistry */ null);
    }

    @After
    public void tearDown() {
        tearDownSecurityManager();
    }

    @Test
    public void testGetRegattas() throws Exception {
        RegattasResource resource = new RegattasResource();
        RegattasResource spyResource = spyResource(resource);

        Response regattasResponse = spyResource.getRegattas();

        String jsonString = (String) regattasResponse.getEntity();
        Object obj = JSONValue.parse(jsonString);
        JSONArray array = (JSONArray) obj;

        assertTrue(array.size() == 2);

        JSONObject firstElement = (JSONObject) array.get(0);
        String jsonName = (String) firstElement.get("name");
        String jsonBoatClass = (String) firstElement.get("boatclass");

        assertTrue(RegattaImpl.getDefaultName(closedRegattaName, boatClassName).equals(jsonName)
                || RegattaImpl.getDefaultName(openRegattaName, boatClassName).equals(jsonName));
        assertTrue(boatClassName.equals(jsonBoatClass));
    }

    @Test
    public void testGetRegatta() throws Exception {
        RegattasResource resource = new RegattasResource();
        RegattasResource spyResource = spyResource(resource);
        final String name = RegattaImpl.getDefaultName(closedRegattaName, boatClassName);
        Response regattaResponse = spyResource.getRegatta(name);
        String jsonString = (String) regattaResponse.getEntity();
        assertNotNull(jsonString);
        String readRegattaName = (String) ((JSONObject) JSONValue.parse(jsonString)).get("name");
        assertEquals(name, readRegattaName);
    }

    @Test
    public void testCompetitorRegistrationByAdmin() throws Exception {
        setUpSecurityManager();
        ThreadState subjectThreadState = setUpSubject("admin");

        RegattasResource resource = new RegattasResource();
        RegattasResource spyResource = spyResource(resource);
        doReturn(securityService).when(spyResource).getService(SecurityService.class);

        User user = new User("admin", "noreply@sapsailing.com", new ArrayList<Account>(0));
        when(securityService.getCurrentUser()).thenReturn(user);

        final String name = RegattaImpl.getDefaultName(closedRegattaName, boatClassName);
        Response reponse = spyResource.createAndAddCompetitor(name, boatClassName, null, "GER", null, null, null,
                "Max Mustermann", null, "abcd-abcd-abcd-abcd-abcd");
        assertTrue(reponse.getStatus() + ": " + reponse.getEntity().toString(), reponse.getStatus() == 200);
        assertTrue(spyResource.getService() == racingEventService);

        Regatta regatta = racingEventService.getRegattaByName(name);

        Iterator<Competitor> cit = regatta.getAllCompetitors().iterator();
        Competitor readCompetitor = cit.next();
        assertNotNull(readCompetitor);

        subjectThreadState.clear();
    }

    @Test
    public void testCompetitorRegistrationAnonymousOnOpenRegatta() throws Exception {
        setUpSecurityManager();

        RegattasResource resource = new RegattasResource();
        RegattasResource spyResource = spyResource(resource);
        doReturn(securityService).when(spyResource).getService(SecurityService.class);

        final String name = RegattaImpl.getDefaultName(openRegattaName, boatClassName);
        Regatta regatta = racingEventService.getRegattaByName(name);

        Response reponse = spyResource.createAndAddCompetitor(name, boatClassName, null, "GER", null, null, null,
                "Max Mustermann", null, "abcd-abcd-abcd-abcd-abcd");
        assertTrue(reponse.getStatus() + ": " + reponse.getEntity().toString(), reponse.getStatus() == 200);
        assertTrue(spyResource.getService() == racingEventService);

        regatta = racingEventService.getRegattaByName(name);
        Iterator<Competitor> cit = regatta.getAllCompetitors().iterator();
        Competitor readCompetitor = cit.next();
        assertNotNull(readCompetitor);
    }

}
