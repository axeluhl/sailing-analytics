package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TrackBasedEstimationWindTrackImpl;

public class WindEstimationOnConstructedTracksTest extends StoredTrackBasedTest {
    private List<Competitor> competitors;
    private static final String[] competitorNames = new String[] { "Wolfgang Hunger", "Dr. Hasso Plattner",  "Robert Stanjek", "Simon Grotelueschen" };
    
    @Before
    public void setUp() {
        competitors = new ArrayList<Competitor>();
        for (String name : competitorNames) {
            competitors.add(createCompetitor(name));
        }
    }
    
    private void initRace(int numberOfCompetitorsToUse, int[] numberOfMarksPassed, TimePoint timePointForFixes) {
        setTrackedRace(createTestTrackedRace("Kieler Woche", "505 Race 2", "505",
                competitors.subList(0, numberOfCompetitorsToUse), timePointForFixes));
        for (int i=0; i<numberOfCompetitorsToUse; i++) {
            initializeMarkPassingForStartGate(competitors.get(i), numberOfMarksPassed[i], timePointForFixes);
        }
    }

    private void initializeMarkPassingForStartGate(Competitor competitor, int numberOfMarksPassed, TimePoint timePointForFixes) {
        TimePoint fixTimePoint = new MillisecondsTimePoint(timePointForFixes.asMillis());
        Set<MarkPassing> markPassingForCompetitor = new HashSet<MarkPassing>();
        int i=0;
        for (Waypoint waypoint : getTrackedRace().getRace().getCourse().getWaypoints()) {
            if (i++ >= numberOfMarksPassed) {
                break;
            }
            markPassingForCompetitor.add(new MarkPassingImpl(fixTimePoint, waypoint, competitor));
            fixTimePoint = new MillisecondsTimePoint(fixTimePoint.asMillis()+1);
        }
        getTrackedRace().updateMarkPassings(competitor, markPassingForCompetitor);
    }

    private CompetitorImpl createCompetitor(String competitorName) {
        return new CompetitorImpl(123, competitorName, new TeamImpl("STG", Collections.singleton(
                new PersonImpl(competitorName, new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */null, "This is famous " + competitorName)), new PersonImpl("Rigo van Maas",
                new NationalityImpl("The Netherlands", "NED"),
                /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                new BoatClassImpl("505", /* typicallyStartsUpwind */true), null));
    }

    private void setBearingForCompetitor(Competitor competitor, TimePoint timePoint, double bearingDeg) {
        DynamicGPSFixTrack<Competitor, GPSFixMoving> competitorTrack = getTrackedRace().getTrack(competitor);
        competitorTrack.addGPSFix(new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), timePoint,
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(bearingDeg))));
    }

    /**
     * See <a href="http://sapcoe-app01.pironet-ndh.com/show_bug.cgi?id=166">bug #166</a>
     */
    @Test
    public void testWindEstimationCacheInvalidationAfterLegTypeChange() throws NoWindException {
        TimePoint fixTime = new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime());
        TimePoint checkTime = new MillisecondsTimePoint(fixTime.asMillis()+60000); // one minute later
        initRace(2, new int[] { 1, 1 }, fixTime);
        getTrackedRace().setRaceIsKnownToStartUpwind(false); // use only WEB wind to determine leg type
        TimePoint now = checkTime;
        // TODO consider adding two more competitors which go the other way and are initially ignored, after leg type change included...
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 50);
        TrackedLeg firstLeg = getTrackedRace().getTrackedLeg(getTrackedRace().getRace().getCourse().getLegs().iterator().next());
        assertEquals(LegType.UPWIND, firstLeg.getLegType(new MillisecondsTimePoint(MillisecondsTimePoint.now().asMillis())));
        final Map<TimePoint, Wind> cachedFixes = new HashMap<TimePoint, Wind>();
        TrackBasedEstimationWindTrackImpl track = new TrackBasedEstimationWindTrackImpl(
                getTrackedRace(), /* millisecondsOverWhichToAverage */ 30000) {
                    @Override
                    protected void cache(TimePoint timePoint, Wind fix) {
                        super.cache(timePoint, fix);
                        if (fix != null) {
                            cachedFixes.put(timePoint, fix);
                        }
                    }
        };
        Wind estimatedWindDirection = track.getAveragedWind(/* position */ null, checkTime);
        assertNotNull(estimatedWindDirection);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
        assertFalse(cachedFixes.isEmpty());
        assertEquals(185., cachedFixes.values().iterator().next().getBearing().getDegrees(), 0.00000001);
        // now invert leg's type by moving the top mark along the wind from the leeward gate:
        Iterator<Waypoint> waypointsIter = getTrackedRace().getRace().getCourse().getWaypoints().iterator();
        Waypoint leewardMark = waypointsIter.next();
        Waypoint windwardMark = waypointsIter.next();
        Position leewardGatePosition = getTrackedRace().getApproximatePosition(leewardMark, checkTime);
        Distance d = new NauticalMileDistance(1);
        Wind wind = getTrackedRace().getWind(null, checkTime, getTrackedRace().getWindSources(WindSourceType.TRACK_BASED_ESTIMATION));
        Position newWindwardMarkPosition = leewardGatePosition.translateGreatCircle(wind.getBearing(), d);
        getTrackedRace().getOrCreateTrack(windwardMark.getBuoys().iterator().next()).addGPSFix(
                new GPSFixImpl(newWindwardMarkPosition, checkTime));
        assertEquals(LegType.DOWNWIND, firstLeg.getLegType(fixTime));
        TimePoint tenMinutesLater = new MillisecondsTimePoint(checkTime.asMillis()+600000l);
        setBearingForCompetitor(competitors.get(0), tenMinutesLater, 140);
        setBearingForCompetitor(competitors.get(1), tenMinutesLater, 230);
        Wind estimatedWindDirectionDownwind = track.getAveragedWind(/* position */ null, tenMinutesLater);
        assertNotNull(estimatedWindDirectionDownwind);
        assertEquals(185., estimatedWindDirectionDownwind.getBearing().getDegrees(), 0.00000001);
    }
    
    @Test
    public void testWindEstimationCaching() {
        MillisecondsTimePoint fixTime = new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime());
        TimePoint checkTime = new MillisecondsTimePoint(fixTime.asMillis()+60000); // one minute later
        initRace(2, new int[] { 1, 1 }, fixTime);
        TimePoint now = checkTime;
        setBearingForCompetitor(competitors.get(0), checkTime, 320);
        setBearingForCompetitor(competitors.get(1), checkTime, 50);
        final Map<TimePoint, Wind> cachedFixes = new HashMap<TimePoint, Wind>();
        TrackBasedEstimationWindTrackImpl track = new TrackBasedEstimationWindTrackImpl(
                getTrackedRace(), /* millisecondsOverWhichToAverage */ 30000) {
                    @Override
                    protected void cache(TimePoint timePoint, Wind fix) {
                        super.cache(timePoint, fix);
                        if (fix != null) {
                            cachedFixes.put(timePoint, fix);
                        }
                    }
        };
        Wind estimatedWindDirection = track.getAveragedWind(/* position */ null, checkTime);
        assertNotNull(estimatedWindDirection);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
        assertFalse(cachedFixes.isEmpty());
        assertEquals(185., cachedFixes.values().iterator().next().getBearing().getDegrees(), 0.00000001);
        // now clear set of cached fixes, ask again and ensure nothing is cached again:
        cachedFixes.clear();
        Wind estimatedWindDirectionCached = track.getAveragedWind(/* position */ null, checkTime);
        assertTrue(cachedFixes.isEmpty());
        assertNotNull(estimatedWindDirectionCached);
        assertEquals(185., estimatedWindDirectionCached.getBearing().getDegrees(), 0.00000001);
        // now add a GPS fix and make sure the cache is invalidated
        now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 330);
        Wind estimatedWindDirectionNew = track.getAveragedWind(/* position */ null, checkTime);
        assertFalse(cachedFixes.isEmpty());
        assertNotNull(estimatedWindDirectionNew);
        assertTrue("Expected estimated wind direction to now be greater than 185 degrees but was "
                + estimatedWindDirectionCached.getBearing().getDegrees(), estimatedWindDirectionCached.getBearing()
                .getDegrees() < 185.); // remember: bearing is opposite of from; boats start with upwind
    }
    
    @Test
    public void testWindEstimationForSimpleTracks() throws NoWindException {
        initRace(2, new int[] { 1, 1 }, new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 50);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testWindEstimationForSimpleTracksWithOneAncientFixToBeSuppressedByLowConfidence() throws NoWindException {
        initRace(3, new int[] { 1, 1, 1 }, new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 50);
        setBearingForCompetitor(competitors.get(2), new MillisecondsTimePoint(0), 100); // this shouldn't disturb the estimation because it's too old
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testWindEstimationForSimpleTracksWithOneFixNearMarkPassingToBeSuppressedByLowConfidence() throws NoWindException {
        TimePoint markPassingTimePoint = new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime());
        initRace(3, new int[] { 1, 1, 1 }, markPassingTimePoint);
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 50);
        setBearingForCompetitor(competitors.get(2), markPassingTimePoint, 100); // this shouldn't disturb the estimation because it's too old
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testWindEstimationForTwoBoatsOnSameTack() throws NoWindException {
        initRace(2, new int[] { 1, 1 }, new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 330); // on the same tack, should give no read-out
        Wind nullWind = getTrackedRace().getEstimatedWindDirection(/* position */null, now);
        assertNull(
                "Shouldn't have been able to determine estimated wind direction because no two distinct direction clusters found upwind nor downwind",
                nullWind);
    }

    @Test
    public void testWindEstimationForFourBoatsOnSameTack() throws NoWindException {
        initRace(4, new int[] { 1, 1, 2, 2 }, new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 330); // on the same tack, should give no read-out
        setBearingForCompetitor(competitors.get(2), now, 135);
        setBearingForCompetitor(competitors.get(3), now, 145); // on the same tack, should give no read-out
        Wind nullWind = getTrackedRace().getEstimatedWindDirection(/* position */null, now);
        assertNull(
                "Shouldn't have been able to determine estimated wind direction because no two distinct direction clusters found upwind nor downwind",
                nullWind);
    }

    @Test
    public void testWindEstimationForFourBoatsWithUpwindOnSameTack() throws NoWindException {
        initRace(4, new int[] { 1, 1, 2, 2 }, new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 330); // on the same tack, should give no read-out
        setBearingForCompetitor(competitors.get(2), now, 135);
        setBearingForCompetitor(competitors.get(3), now, 220); // on the same tack, should give no read-out
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(177.5, estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testWindEstimationForFourBoats() throws NoWindException {
        initRace(4, new int[] { 1, 1, 2, 2 }, new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 315);
        setBearingForCompetitor(competitors.get(1), now, 50); // on the same tack, should give no read-out
        setBearingForCompetitor(competitors.get(2), now, 135);
        setBearingForCompetitor(competitors.get(3), now, 220); // on the same tack, should give no read-out
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(180., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

}
