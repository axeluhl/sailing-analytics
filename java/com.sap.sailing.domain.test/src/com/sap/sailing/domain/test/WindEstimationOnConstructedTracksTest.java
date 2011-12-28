package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.Wind;
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
    
    private void initRace(int numberOfCompetitorsToUse, int[] numberOfMarksPassed) {
        setTrackedRace(createTestTrackedRace("Kieler Woche", "505 Race 2", "505",
                competitors.subList(0, numberOfCompetitorsToUse)));
        for (int i=0; i<numberOfCompetitorsToUse; i++) {
            initializeMarkPassingForStartGate(competitors.get(i), numberOfMarksPassed[i]);
        }
    }

    private void initializeMarkPassingForStartGate(Competitor competitor, int numberOfMarksPassed) {
        Set<MarkPassing> markPassingForCompetitor = new HashSet<MarkPassing>();
        int i=0;
        for (Waypoint waypoint : getTrackedRace().getRace().getCourse().getWaypoints()) {
            if (i++ >= numberOfMarksPassed) {
                break;
            }
            markPassingForCompetitor.add(new MarkPassingImpl(MillisecondsTimePoint.now(), waypoint, competitor));
            try {
                Thread.sleep(1); // ensure the next time point is at least one ms later
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        DynamicGPSFixTrack<Competitor, GPSFixMoving> hungersTrack = getTrackedRace().getTrack(competitor);
        hungersTrack.addGPSFix(new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), timePoint,
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(bearingDeg))));
    }

    @Test
    public void testWindEstimationCaching() {
        initRace(2, new int[] { 1, 1 });
        TimePoint now = getTrackedRace()
                .getMarkPassingsInOrder(getTrackedRace().getRace().getCourse().getFirstWaypoint()).iterator().next()
                .getTimePoint();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 50);
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
        Wind estimatedWindDirection = track.getEstimatedWind(/* position */ null, now);
        assertNotNull(estimatedWindDirection);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
        assertFalse(cachedFixes.isEmpty());
        assertEquals(185., cachedFixes.values().iterator().next().getBearing().getDegrees(), 0.00000001);
        // now clear set of cached fixes, ask again and ensure nothing is cached again:
        cachedFixes.clear();
        Wind estimatedWindDirectionCached = track.getEstimatedWind(/* position */ null, now);
        assertTrue(cachedFixes.isEmpty());
        assertNotNull(estimatedWindDirectionCached);
        assertEquals(185., estimatedWindDirectionCached.getBearing().getDegrees(), 0.00000001);
        // now add a GPS fix and make sure the cache is invalidated
        now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 330);
        Wind estimatedWindDirectionNew = track.getEstimatedWind(/* position */ null, now);
        assertFalse(cachedFixes.isEmpty());
        assertNotNull(estimatedWindDirectionNew);
        assertTrue("Expected estimated wind direction to now be greater than 185 degrees but was "
                + estimatedWindDirectionCached.getBearing().getDegrees(), estimatedWindDirectionCached.getBearing()
                .getDegrees() < 185.); // remember: bearing is opposite of from; boats start with upwind
    }
    
    @Test
    public void testWindEstimationForSimpleTracks() throws NoWindException {
        initRace(2, new int[] { 1, 1 });
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 320);
        setBearingForCompetitor(competitors.get(1), now, 50);
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(185., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testWindEstimationForTwoBoatsOnSameTack() throws NoWindException {
        initRace(2, new int[] { 1, 1 });
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
        initRace(4, new int[] { 1, 1, 2, 2 });
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
        initRace(4, new int[] { 1, 1, 2, 2 });
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
        initRace(4, new int[] { 1, 1, 2, 2 });
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        setBearingForCompetitor(competitors.get(0), now, 315);
        setBearingForCompetitor(competitors.get(1), now, 50); // on the same tack, should give no read-out
        setBearingForCompetitor(competitors.get(2), now, 135);
        setBearingForCompetitor(competitors.get(3), now, 220); // on the same tack, should give no read-out
        Wind estimatedWindDirection = getTrackedRace().getEstimatedWindDirection(/* position */ null, now);
        assertEquals(180., estimatedWindDirection.getBearing().getDegrees(), 0.00000001);
    }

}
