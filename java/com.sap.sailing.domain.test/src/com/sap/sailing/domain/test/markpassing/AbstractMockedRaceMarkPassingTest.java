package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRegattaImpl;

public class AbstractMockedRaceMarkPassingTest {

    private DynamicTrackedRace trackedRace;
    protected long time;
    protected Random rnd = new Random();

    public AbstractMockedRaceMarkPassingTest() {
    }

    public DynamicTrackedRace getRace(){
        return trackedRace;
    }
    protected void setUp(Iterable<Waypoint> waypoints, Iterable<Competitor> competitors) {
        
        Regatta r = new RegattaImpl("regatta", new BoatClassImpl("boat", true), Arrays.asList(new SeriesImpl("Series",
                true, Arrays.asList(new FleetImpl("fleet")), new ArrayList<String>(), null)), true, new HighPoint(),
                "ID", new CourseAreaImpl("area", new UUID(5, 5)));
        Course course = new CourseImpl("course", waypoints);
        RaceDefinition race = new RaceDefinitionImpl("Performance Race", course, new BoatClassImpl("boat", true),
                competitors);
        trackedRace = new DynamicTrackedRaceImpl(new TrackedRegattaImpl(r), race, new ArrayList<Sideline>(),
                new EmptyWindStore(), 0, 10000, 10000);
    }
}
