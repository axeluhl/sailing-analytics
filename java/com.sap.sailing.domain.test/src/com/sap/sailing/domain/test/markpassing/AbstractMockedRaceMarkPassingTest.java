package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbstractMockedRaceMarkPassingTest {
    protected Competitor bob = new CompetitorImpl("Bob", "Bob", null, null, null);
    protected Competitor joe = new CompetitorImpl("Joe", "Joe", null, null, null);
    protected Competitor mike = new CompetitorImpl("Mike", "Mike", null, null, null);

    protected Mark m = new MarkImpl("black");
    protected Mark m2 = new MarkImpl("white 1");
    protected Mark m3 = new MarkImpl("white 2");

    protected List<Waypoint> waypoints = new ArrayList<>();
    protected DynamicTrackedRace trackedRace;
    protected long time;
    protected Random rnd = new Random();

    public AbstractMockedRaceMarkPassingTest() {
        ControlPointWithTwoMarks cp = new ControlPointWithTwoMarksImpl(m2, m3, "cp");
        Waypoint w1 = new WaypointImpl(cp, PassingInstruction.Line);
        Waypoint w2 = new WaypointImpl(m, PassingInstruction.Port);
        Waypoint w3 = new WaypointImpl(cp, PassingInstruction.Line);
        final BoatClassImpl boatClass = new BoatClassImpl("boat", true);
        Regatta r = new RegattaImpl(RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass, Arrays.asList(new SeriesImpl("Series", true, Arrays.asList(new FleetImpl("fleet")),
                new ArrayList<String>(), null)), true, new HighPoint(), "ID", new CourseAreaImpl("area", new UUID(5, 5)));
        Course course = new CourseImpl("course", Arrays.asList(w1, w2, w3));
        RaceDefinition race = new RaceDefinitionImpl("Performance Race", course, boatClass, Arrays.asList(bob));
        trackedRace = new DynamicTrackedRaceImpl(new DynamicTrackedRegattaImpl(r), race, new ArrayList<Sideline>(), new EmptyWindStore(), EmptyGPSFixStore.INSTANCE, 0, 10000, 10000);
        trackedRace.setStartTimeReceived(new MillisecondsTimePoint(System.currentTimeMillis() - 120000));
        List<MillisecondsTimePoint> tps = Arrays.asList(new MillisecondsTimePoint(System.currentTimeMillis()), new MillisecondsTimePoint(System.currentTimeMillis() - 30000),
                new MillisecondsTimePoint(System.currentTimeMillis() + 30000));
        List<Util.Pair<Mark, Position>> pos = Arrays.asList(new Util.Pair<Mark, Position>(m, new DegreePosition(0, 0)), new Util.Pair<Mark, Position>(m2, new DegreePosition(-0.001, -0.00005)),
                new Util.Pair<Mark, Position>(m3, new DegreePosition(-0.001, 0.00005)));
        for (Util.Pair<Mark, Position> pair : pos) {
            for (TimePoint t : tps) {
                trackedRace.recordFix(pair.getA(), new GPSFixImpl(pair.getB(), t));
            }
        }
        for (Waypoint w : trackedRace.getRace().getCourse().getWaypoints()) {
            waypoints.add(w);
        }
    }

    protected GPSFixMoving rndFix() {
        DegreePosition position = new DegreePosition(37.8878 + rnd.nextDouble() * 0.0019, -122.268 - rnd.nextDouble() * 0.012);
        TimePoint p = new MillisecondsTimePoint((long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000))));
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(rnd.nextInt(11), new DegreeBearingImpl(rnd.nextInt(360)));

        return new GPSFixMovingImpl(position, p, speed);
    }
}
