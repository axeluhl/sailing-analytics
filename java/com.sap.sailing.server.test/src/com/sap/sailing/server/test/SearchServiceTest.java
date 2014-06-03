package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.test.AbstractTracTracLiveTest;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RegattaSearchResult;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.RemoveEvent;
import com.sap.sailing.server.operationaltransformation.RemoveRegatta;
import com.sap.sse.common.Util;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;

public class SearchServiceTest {
    private RacingEventService server;
    private Venue kiel;
    private Venue flensburg;
    private Event pfingstbusch;
    private Regatta pfingstbusch29er;
    private Competitor hassoPlattner;
    private Competitor alexanderRies;
    private Competitor antonKoch;
    private Competitor tobiasSchadewaldt;
    private Competitor philippBuhl;
    private Competitor dennisGehrlein;
    private Regatta pfingstbusch470;
    private Regatta aalRegatta;
    private DynamicTrackedRace pfingstbusch29erTrackedR1;
    private DynamicTrackedRace pfingstbusch29erTrackedR3;
    private Event aalEvent;
    private DynamicTrackedRace pfingstbusch470TrackedR1;
    private DynamicTrackedRace pfingstbusch470TrackedR2;
    private DynamicTrackedRace aalOrcTrackedR1;
    private DynamicTrackedRace aalOrcTrackedR2;

    @Before
    public void setUp() {
        server = new RacingEventServiceImpl();
        List<Event> allEvents = new ArrayList<>();
        Util.addAll(server.getAllEvents(), allEvents);
        for (Event e : allEvents) {
            server.apply(new RemoveEvent(e.getId()));
        }
        server.apply(new RemoveRegatta(new RegattaName("Pfingstbusch (29er)")));
        server.apply(new RemoveRegatta(new RegattaName("Pfingstbusch (470)")));
        server.apply(new RemoveRegatta(new RegattaName("Aalregatta (ORC)")));
        final Calendar cal = new GregorianCalendar();
        cal.set(2014, 5, 6, 10, 00);
        final TimePoint pfingstbuschStartDate = new MillisecondsTimePoint(cal.getTime());
        cal.set(2014, 5, 8, 16, 00);
        final TimePoint pfingstbuschEndDate = new MillisecondsTimePoint(cal.getTime());
        pfingstbusch = server.apply(new CreateEvent("Pfingsbusch", pfingstbuschStartDate, pfingstbuschEndDate, "Kiel", /* isPublic */
                true, UUID.randomUUID(), Collections.<URL>emptySet(), Collections.<URL>emptySet()));
        kiel = pfingstbusch.getVenue();
        final CourseAreaImpl kielAlpha = new CourseAreaImpl("Alpha", UUID.randomUUID());
        kiel.addCourseArea(kielAlpha);
        final CourseAreaImpl kielBravo = new CourseAreaImpl("Bravo", UUID.randomUUID());
        kiel.addCourseArea(kielBravo);
        pfingstbusch29er = server.apply(new AddSpecificRegatta("Pfingstbusch", "29er", UUID.randomUUID(),
                new RegattaCreationParametersDTO(new LinkedHashMap<String, SeriesCreationParametersDTO>()), /* persistent */
                true, new LowPoint(), kielAlpha.getId()));
        pfingstbusch470 = server.apply(new AddSpecificRegatta("Pfingstbusch", "470", UUID.randomUUID(),
                new RegattaCreationParametersDTO(new LinkedHashMap<String, SeriesCreationParametersDTO>()), /* persistent */
                true, new LowPoint(), kielBravo.getId()));
        cal.set(2014, 5, 7, 10, 00);
        final TimePoint aalStartDate = new MillisecondsTimePoint(cal.getTime());
        cal.set(2014, 5, 8, 18, 00);
        final TimePoint aalEndDate = new MillisecondsTimePoint(cal.getTime());
        aalEvent = server.apply(new CreateEvent("Aalregatta", aalStartDate, aalEndDate, "Flensburg", /* isPublic */
                true, UUID.randomUUID(), Collections.<URL>emptySet(), Collections.<URL>emptySet()));
        flensburg = aalEvent.getVenue();
        final CourseAreaImpl flensburgStandard = new CourseAreaImpl("Standard", UUID.randomUUID());
        flensburg.addCourseArea(flensburgStandard);
        aalRegatta = server.apply(new AddSpecificRegatta("Aalregatta", "ORC", UUID.randomUUID(),
                new RegattaCreationParametersDTO(new LinkedHashMap<String, SeriesCreationParametersDTO>()), /* persistent */
                true, new LowPoint(), flensburgStandard.getId()));
        hassoPlattner = AbstractTracTracLiveTest.createCompetitor("Hasso Plattner");
        alexanderRies = AbstractTracTracLiveTest.createCompetitor("Alexander Ries");
        antonKoch = AbstractTracTracLiveTest.createCompetitor("Anton Koch");
        tobiasSchadewaldt = AbstractTracTracLiveTest.createCompetitor("Tobias Schadewaldt");
        philippBuhl = AbstractTracTracLiveTest.createCompetitor("Philipp Buhl");
        dennisGehrlein = AbstractTracTracLiveTest.createCompetitor("Dennis Gehrlein");
        final RaceDefinitionImpl pfingstbusch29erR1 = new RaceDefinitionImpl("R1", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), pfingstbusch29er.getBoatClass(),
                Arrays.asList(new Competitor[] { alexanderRies, tobiasSchadewaldt }));
        final RaceDefinitionImpl pfingstbusch29erR2 = new RaceDefinitionImpl("R2", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), pfingstbusch29er.getBoatClass(),
                Arrays.asList(new Competitor[] { alexanderRies, tobiasSchadewaldt }));
        final RaceDefinitionImpl pfingstbusch29erR3 = new RaceDefinitionImpl("R3", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), pfingstbusch29er.getBoatClass(),
                Arrays.asList(new Competitor[] { alexanderRies, tobiasSchadewaldt }));
        server.apply(new AddRaceDefinition(pfingstbusch29er.getRegattaIdentifier(), pfingstbusch29erR1));
        server.apply(new AddRaceDefinition(pfingstbusch29er.getRegattaIdentifier(), pfingstbusch29erR2));
        server.apply(new AddRaceDefinition(pfingstbusch29er.getRegattaIdentifier(), pfingstbusch29erR3));
        // track only R1 and R3
        pfingstbusch29erTrackedR1 = server.apply(new CreateTrackedRace(pfingstbusch29er.getRaceIdentifier(pfingstbusch29erR1), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, /* delayToLiveInMillis */ 3000,
                /* millisecondsOverWhichToAverageWind */ 15000, /* millisecondsOverWhichToAverageSpeed */ 15000));
        pfingstbusch29erTrackedR1.setStartOfTrackingReceived(pfingstbuschStartDate.plus(1));
        pfingstbusch29erTrackedR3 = server.apply(new CreateTrackedRace(pfingstbusch29er.getRaceIdentifier(pfingstbusch29erR3), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, /* delayToLiveInMillis */ 3000,
                /* millisecondsOverWhichToAverageWind */ 15000, /* millisecondsOverWhichToAverageSpeed */ 15000));

        final RaceDefinitionImpl pfingstbush470R1 = new RaceDefinitionImpl("R1", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), pfingstbusch470.getBoatClass(),
                Arrays.asList(new Competitor[] { philippBuhl, antonKoch }));
        final RaceDefinitionImpl pfingstbush470R2 = new RaceDefinitionImpl("R2", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), pfingstbusch470.getBoatClass(),
                Arrays.asList(new Competitor[] { philippBuhl, antonKoch }));
        server.apply(new AddRaceDefinition(pfingstbusch470.getRegattaIdentifier(), pfingstbush470R1));
        server.apply(new AddRaceDefinition(pfingstbusch470.getRegattaIdentifier(), pfingstbush470R2));
        // track only R1 and R2
        pfingstbusch470TrackedR1 = server.apply(new CreateTrackedRace(pfingstbusch470.getRaceIdentifier(pfingstbush470R1), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, /* delayToLiveInMillis */ 3000,
                /* millisecondsOverWhichToAverageWind */ 15000, /* millisecondsOverWhichToAverageSpeed */ 15000));
        pfingstbusch470TrackedR1.setStartOfTrackingReceived(pfingstbuschStartDate.plus(2)); // starts later than 29er
        pfingstbusch470TrackedR2 = server.apply(new CreateTrackedRace(pfingstbusch470.getRaceIdentifier(pfingstbush470R2), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, /* delayToLiveInMillis */ 3000,
                /* millisecondsOverWhichToAverageWind */ 15000, /* millisecondsOverWhichToAverageSpeed */ 15000));

        final RaceDefinitionImpl aalOrcR1 = new RaceDefinitionImpl("R1", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), aalRegatta.getBoatClass(),
                Arrays.asList(new Competitor[] { hassoPlattner, dennisGehrlein, philippBuhl }));
        final RaceDefinitionImpl aalOrcR2 = new RaceDefinitionImpl("R2", new CourseImpl("up/down", Collections.<Waypoint>emptyList()), aalRegatta.getBoatClass(),
                Arrays.asList(new Competitor[] { hassoPlattner, dennisGehrlein, philippBuhl }));
        server.apply(new AddRaceDefinition(aalRegatta.getRegattaIdentifier(), aalOrcR1));
        server.apply(new AddRaceDefinition(aalRegatta.getRegattaIdentifier(), aalOrcR2));
        // track only R1 and R2
        aalOrcTrackedR1 = server.apply(new CreateTrackedRace(aalRegatta.getRaceIdentifier(aalOrcR1), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, /* delayToLiveInMillis */ 3000,
                /* millisecondsOverWhichToAverageWind */ 15000, /* millisecondsOverWhichToAverageSpeed */ 15000));
        aalOrcTrackedR1.setStartOfTrackingReceived(aalStartDate);
        aalOrcTrackedR2 = server.apply(new CreateTrackedRace(aalRegatta.getRaceIdentifier(aalOrcR2), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE, /* delayToLiveInMillis */ 3000,
                /* millisecondsOverWhichToAverageWind */ 15000, /* millisecondsOverWhichToAverageSpeed */ 15000));
    }
    
    @Test
    public void testSetup() {
        assertNotNull(pfingstbusch29er);
        assertNotNull(pfingstbusch29erTrackedR1);
        assertNotNull(pfingstbusch29erTrackedR3);
        assertNotNull(pfingstbusch470TrackedR1);
        assertNotNull(pfingstbusch470TrackedR2);
        assertNotNull(aalEvent);
        assertNotNull(aalRegatta);
        assertNotNull(aalOrcTrackedR1);
        assertNotNull(aalOrcTrackedR2);
    }
    
    @Test
    public void testSimpleSearchByCompetitorName() {
        Result<RegattaSearchResult> searchResults = server.search(new KeywordQuery(Arrays.asList(new String[] { "Tobi" })));
        assertEquals(1, Util.size(searchResults.getHits()));
        Regatta foundRegatta = searchResults.getHits().iterator().next().getRegatta();
        assertSame(pfingstbusch29er, foundRegatta);
    }

    @Test
    public void testSimpleSearchByCompetitorName2() {
        Result<RegattaSearchResult> searchResults = server.search(new KeywordQuery(Arrays.asList(new String[] { "Hasso" })));
        assertEquals(1, Util.size(searchResults.getHits()));
        Regatta foundRegatta = searchResults.getHits().iterator().next().getRegatta();
        assertSame(aalRegatta, foundRegatta);
    }

    @Test
    public void testSimpleSearchByVenueName() {
        Result<RegattaSearchResult> searchResults = server.search(new KeywordQuery(Arrays.asList(new String[] { "Flensburg" })));
        assertEquals(1, Util.size(searchResults.getHits()));
        Regatta foundRegatta = searchResults.getHits().iterator().next().getRegatta();
        assertSame(aalRegatta, foundRegatta);
    }

    @Test
    public void testSimpleSearchByVenueName2() {
        Result<RegattaSearchResult> searchResults = server.search(new KeywordQuery(Arrays.asList(new String[] { "Kiel" })));
        assertEquals(2, Util.size(searchResults.getHits()));
        final Iterator<RegattaSearchResult> iterator = searchResults.getHits().iterator();
        Regatta firstFoundRegatta = iterator.next().getRegatta();
        assertSame(pfingstbusch29er, firstFoundRegatta);
        Regatta secondFoundRegatta = iterator.next().getRegatta();
        assertSame(pfingstbusch470, secondFoundRegatta);
    }

    @Test
    public void testMultipleMatchesSortedCorrectly() {
        Result<RegattaSearchResult> searchResults = server.search(new KeywordQuery(Arrays.asList(new String[] { "Buhl" })));
        assertEquals(2, Util.size(searchResults.getHits()));
        final Iterator<RegattaSearchResult> iter = searchResults.getHits().iterator();
        Regatta earlierStartRegatta = iter.next().getRegatta();
        assertSame(pfingstbusch470, earlierStartRegatta);
        Regatta laterStartRegatta = iter.next().getRegatta();
        assertSame(aalRegatta, laterStartRegatta);
    }
}
