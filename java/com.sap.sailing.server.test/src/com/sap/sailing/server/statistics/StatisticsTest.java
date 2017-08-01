package com.sap.sailing.server.statistics;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StatisticsTest {
    private static final long START_OF_TRACKING = 100;
    private static final long START_OF_RACE = 200;
    private static final long END_OF_TRACKING = 400;
    private final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");
    protected final Competitor comp = DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null,
            null, null, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    protected final Mark mark = DomainFactory.INSTANCE.getOrCreateMark("mark");
    protected final Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
    protected final AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);
    private DynamicTrackedRegatta regatta;
    private DynamicTrackedRace trackedRace;

    @Before
    public void setUp() {
        regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass,
                /* startDate */ null, /* endDate */null, null, null, "a", null));

        Course course = new CourseImpl("course",
                Arrays.asList(new Waypoint[] { new WaypointImpl(mark), new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(comp));

        trackedRace = new DynamicTrackedRaceImpl(regatta, race, Collections.<Sideline>emptyList(),
                EmptyWindStore.INSTANCE, 0, 0, 0, /* useMarkPassingCalculator */ false, OneDesignRankingMetric::new,
                mock(RaceLogResolver.class));
        trackedRace.setStartOfTrackingReceived(new MillisecondsTimePoint(START_OF_TRACKING));
        trackedRace.setEndOfTrackingReceived(new MillisecondsTimePoint(END_OF_TRACKING));
        trackedRace.setStartTimeReceived(new MillisecondsTimePoint(START_OF_RACE));

        regatta.addTrackedRace(trackedRace);
    }

    private TrackedRaceStatisticsCacheImpl getStatisticsCacheWithRegattaAdded() {
        TrackedRaceStatisticsCacheImpl trackedRaceStatisticsCache = new TrackedRaceStatisticsCacheImpl();
        trackedRaceStatisticsCache.regattaAdded(regatta);
        return trackedRaceStatisticsCache;
    }

    @Test
    public void testTrackedRaceStatisticsCacheWithoutFixes() {
        TrackedRaceStatisticsCacheImpl trackedRaceStatisticsCache = getStatisticsCacheWithRegattaAdded();

        TrackedRaceStatistics statisticsForRace = trackedRaceStatisticsCache.getStatisticsWaitingForLatest(trackedRace);

        assertEquals(0, statisticsForRace.getNumberOfWindFixes());
        assertEquals(0, statisticsForRace.getNumberOfGPSFixes());
    }

    @Test
    public void testTrackedRaceStatisticsCacheWithFixes() {
        trackedRace.recordFix(comp,
                new GPSFixMovingImpl(new DegreePosition(49.295970, 8.638958), new MillisecondsTimePoint(START_OF_RACE),
                        new KilometersPerHourSpeedWithBearingImpl(1, new DegreeBearingImpl(100))));
        trackedRace.recordFix(comp,
                new GPSFixMovingImpl(new DegreePosition(49.295911, 8.638971),
                        new MillisecondsTimePoint(START_OF_RACE + 10),
                        new KilometersPerHourSpeedWithBearingImpl(1, new DegreeBearingImpl(100))));
        trackedRace.recordFix(comp,
                new GPSFixMovingImpl(new DegreePosition(49.295866, 8.638986),
                        new MillisecondsTimePoint(START_OF_RACE + 20),
                        new KilometersPerHourSpeedWithBearingImpl(1, new DegreeBearingImpl(100))));
        trackedRace.recordFix(comp,
                new GPSFixMovingImpl(new DegreePosition(49.295822, 8.639010),
                        new MillisecondsTimePoint(START_OF_RACE + 30),
                        new KilometersPerHourSpeedWithBearingImpl(1, new DegreeBearingImpl(100))));
        trackedRace.recordFix(comp,
                new GPSFixMovingImpl(new DegreePosition(49.295785, 8.639057),
                        new MillisecondsTimePoint(START_OF_RACE + 40),
                        new KilometersPerHourSpeedWithBearingImpl(1, new DegreeBearingImpl(100))));

        TrackedRaceStatisticsCacheImpl trackedRaceStatisticsCache = getStatisticsCacheWithRegattaAdded();

        TrackedRaceStatistics statisticsForRace = trackedRaceStatisticsCache.getStatisticsWaitingForLatest(trackedRace);

        assertEquals(5, statisticsForRace.getNumberOfGPSFixes());
    }

}
