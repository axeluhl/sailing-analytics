package com.sap.sailing.domain.racelog.tracking.test.impl;

import static com.sap.sailing.domain.common.impl.Util.size;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelog.tracking.impl.RaceLogTrackingAdapterFactoryImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockServiceFinderFactory;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class CreateAndTrackWithRaceLogTest {
    private RacingEventService service;

    private final Fleet fleet = new FleetImpl("fleet");
    private final String columnName = "column";
    private RegattaLeaderboard leaderboard;
    private RaceLogTrackingAdapter adapter;
    private Regatta regatta;
    private RaceLogEventAuthor author;
    private GPSFixStore gpsFixStore;

    private long time = 0;

    private static final RaceLogEventFactory factory = RaceLogEventFactory.INSTANCE;

    @Before
    public void setup() {
        service = new RacingEventServiceImpl(true, new MockServiceFinderFactory());
        gpsFixStore = service.getGPSFixStore();
        service.getMongoObjectFactory().getDatabase().dropDatabase();
        author = service.getServerAuthor();
        Series series = new SeriesImpl("series", false, Collections.singletonList(fleet),
                Collections.singletonList(columnName), service);
        regatta = service.createRegatta("regatta", "Laser", UUID.randomUUID(), Collections.<Series>singletonList(series),
                false, new HighPoint(), UUID.randomUUID());
        leaderboard = service.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "RegattaLeaderboard", new int[] {});
        adapter = RaceLogTrackingAdapterFactoryImpl.INSTANCE.getAdapter(DomainFactory.INSTANCE);
    }

    @Test
    public void hasRaceLog() {
        assertNotNull(leaderboard.getRaceColumnByName(columnName).getRaceLog(fleet));
    }

    private TimePoint t() {
        return new MillisecondsTimePoint(time++);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Test
    public void cantAddBeforeDenoting() throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception {
        RaceColumn column = leaderboard.getRaceColumnByName(columnName);		
        exception.expect(RaceNotCreatedException.class);
        adapter.addRace(service, regatta.getRegattaIdentifier(), leaderboard, column, fleet, -1);
    }

    private void testSize(Track<?> track, int expected) {
        track.lockForRead();
        assertEquals(expected, size(track.getRawFixes()));
        track.unlockAfterRead();
    }

    @Test
    public void canDenote_Add_Track() throws MalformedURLException, FileNotFoundException, URISyntaxException, Exception {

        RaceColumn column = leaderboard.getRaceColumnByName(columnName);
        RaceLog raceLog = column.getRaceLog(fleet);

        //before denoting, it is not listed as loadable
        Map<RaceColumn, Collection<Fleet>> loadable = adapter.listRaceLogTrackersThatCanBeAdded(service, leaderboard);
        assertEquals(0, loadable.size());

        //can denote racelog for tracking
        assertTrue(raceLog.isEmpty());
        adapter.denoteForRaceLogTracking(service, leaderboard, column, fleet, "race");
        assertFalse(raceLog.isEmpty());

        //add a mapping and one fix in, one out of mapping
        Competitor comp1 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp1", "comp1", null, null, null);
        DeviceIdentifier dev1 = new SmartphoneImeiIdentifier("dev1");
        raceLog.add(factory.createDeviceCompetitorMappingEvent(t(), author, dev1, comp1, 0, new MillisecondsTimePoint(0), new MillisecondsTimePoint(10)));
        gpsFixStore.storeFix(dev1, new GPSFixMovingImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(5), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(5))));
        gpsFixStore.storeFix(dev1, new GPSFixMovingImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(15), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(5))));

        //then listed as loadable
        loadable = adapter.listRaceLogTrackersThatCanBeAdded(service, leaderboard);
        assertEquals(1, loadable.size());
        assertEquals(1, loadable.get(column).size());

        //now can add
        RacesHandle handle = adapter.addRace(service, regatta.getRegattaIdentifier(), leaderboard, column, fleet, -1);

        //no trackedrace yet
        assertNull(column.getTrackedRace(fleet));

        CourseBase course = new CourseDataImpl("test");
        course.addWaypoint(0, new WaypointImpl(new MarkImpl("mark")));
        raceLog.add(factory.createCourseDesignChangedEvent(t(), author, 0, course));

        //start tracking
        raceLog.add(factory.createStartTrackingEvent(t(), author, 0));

        //now there is a trackedrace
        TrackedRace race = column.getTrackedRace(fleet);
        assertNotNull(race);

        //one fix should have been loaded from store
        testSize(race.getTrack(comp1), 1);

        //further fix arrives in race
        gpsFixStore.storeFix(dev1, new GPSFixMovingImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(7), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(5))));
        gpsFixStore.storeFix(dev1, new GPSFixMovingImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(14), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(5)))); //outside mapping range
        testSize(race.getTrack(comp1), 2);

        //add another mapping on the fly, other old fixes should be loaded
        raceLog.add(factory.createDeviceCompetitorMappingEvent(t(), author, dev1, comp1, 0, new MillisecondsTimePoint(11), new MillisecondsTimePoint(20)));
        testSize(race.getTrack(comp1), 4);

        //add another fix in new mapping range
        gpsFixStore.storeFix(dev1, new GPSFixMovingImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(18), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(5))));
        testSize(race.getTrack(comp1), 5);

        //stop tracking, then no more fixes arrive at race
        handle.getRaceTracker().stop();
        gpsFixStore.storeFix(dev1, new GPSFixMovingImpl(new DegreePosition(0, 0), new MillisecondsTimePoint(8), new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(5))));
        testSize(race.getTrack(comp1), 5);
    }
}
