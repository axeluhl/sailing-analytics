package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.TrackedEventImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;

public class ReachingLegTest extends TrackBasedTest {
    private List<Competitor> competitors;
    private CompetitorImpl plattner;
    private CompetitorImpl hunger;
    private MillisecondsTimePoint start;

    /**
     * Creates the race and two competitors ({@link #plattner} and {@link #hunger}) and sets the start line passing for both of them
     * to {@link #start}.
     */
    @Before
    public void setUp() {
        competitors = new ArrayList<Competitor>();
        hunger = createCompetitor("Wolfgang Hunger");
        competitors.add(hunger);
        plattner = createCompetitor("Dr. Hasso Plattner");
        competitors.add(plattner);
        start = new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime());
        setTrackedRace(createTestTrackedRace("Kieler Woche", "505 Race 2", "505", competitors, start));
        List<MarkPassing> hungersMarkPassings = createMarkPassings(hunger, start);
        getTrackedRace().updateMarkPassings(hunger, hungersMarkPassings);
        List<MarkPassing> plattnersMarkPassings = createMarkPassings(plattner, start);
        getTrackedRace().updateMarkPassings(plattner, plattnersMarkPassings);
    }
    
    protected DynamicTrackedRace createTestTrackedRace(String eventName, String raceName, String boatClassName,
            Iterable<Competitor> competitors, TimePoint timePointForFixes) {
        BoatClassImpl boatClass = new BoatClassImpl(boatClassName, /* typicallyStartsUpwind */ true);
        Event event = new EventImpl(eventName, boatClass);
        TrackedEvent trackedEvent = new TrackedEventImpl(event);
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        // create a two-lap upwind/downwind course:
        BuoyImpl left = new BuoyImpl("Left lee gate buoy");
        BuoyImpl right = new BuoyImpl("Right lee gate buoy");
        ControlPoint leeGate = new GateImpl(left, right, "Lee Gate");
        Buoy windwardMark = new BuoyImpl("Windward mark");
        Buoy offsetMark = new BuoyImpl("Offset mark");
        waypoints.add(new WaypointImpl(leeGate));
        waypoints.add(new WaypointImpl(windwardMark));
        waypoints.add(new WaypointImpl(offsetMark));
        waypoints.add(new WaypointImpl(leeGate));
        Course course = new CourseImpl(raceName, waypoints);
        RaceDefinition race = new RaceDefinitionImpl(raceName, course, boatClass, competitors);
        DynamicTrackedRace trackedRace = new DynamicTrackedRaceImpl(trackedEvent, race, EmptyWindStore.INSTANCE,
                /* millisecondsOverWhichToAverageWind */ 30000, /* millisecondsOverWhichToAverageSpeed */ 30000,
                /* delay for wind estimation cache invalidation */ 0);
        // in this simplified artificial course, the top mark is exactly north of the right leeward gate, the offset
        // mark is slightly west of the top mark; wind from the north makes the leg from top to offset a reaching leg
        Position leftPosition = new DegreePosition(0, -0.00001);
        Position rightPosition = new DegreePosition(0, 0.00001);
        Position topPosition = new DegreePosition(1, 0);
        Position offsetPosition = new DegreePosition(1, -0.000001);
        TimePoint afterTheRace = new MillisecondsTimePoint(timePointForFixes.asMillis() + 36000000); // 10h after the fix time
        trackedRace.getOrCreateTrack(left).addGPSFix(new GPSFixImpl(leftPosition, new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(right).addGPSFix(new GPSFixImpl(rightPosition, new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(offsetMark).addGPSFix(new GPSFixImpl(offsetPosition, new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(left).addGPSFix(new GPSFixImpl(leftPosition, afterTheRace));
        trackedRace.getOrCreateTrack(right).addGPSFix(new GPSFixImpl(rightPosition, afterTheRace));
        trackedRace.getOrCreateTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, afterTheRace));
        trackedRace.getOrCreateTrack(offsetMark).addGPSFix(new GPSFixImpl(offsetPosition, afterTheRace));
        trackedRace.recordWind(new WindImpl(topPosition, timePointForFixes, new KnotSpeedWithBearingImpl(
                /* speedInKnots */14.7, new DegreeBearingImpl(180))), new WindSourceImpl(WindSourceType.WEB));
        return trackedRace;
    }

    @Test
    public void testSecondLegIsReaching() throws NoWindException {
        Iterator<TrackedLeg> legIter = getTrackedRace().getTrackedLegs().iterator();
        assertEquals(LegType.UPWIND, legIter.next().getLegType(start));
        assertEquals(LegType.REACHING, legIter.next().getLegType(start));
        assertEquals(LegType.DOWNWIND, legIter.next().getLegType(start));
    }
    
    @Test
    public void testHungerInReachingPlattnerInUpwind() {
        // give Hunger a mark passing for the windward mark, putting him into the reaching leg
        final MillisecondsTimePoint whenHungerFinishedUpwind = new MillisecondsTimePoint(start.asMillis()+600000);
        getTrackedRace().updateMarkPassings(hunger, createMarkPassings(hunger, start, whenHungerFinishedUpwind));
        assertEquals(
                getTrackedRace().getRace().getCourse().getLegs().get(1),
                getTrackedRace().getTrackedLeg(hunger,
                        new MillisecondsTimePoint(whenHungerFinishedUpwind.asMillis() + 10000)).getLeg());
        assertEquals(
                getTrackedRace().getRace().getCourse().getLegs().get(0),
                getTrackedRace().getTrackedLeg(plattner,
                        new MillisecondsTimePoint(whenHungerFinishedUpwind.asMillis() + 10000)).getLeg());
    }
}
