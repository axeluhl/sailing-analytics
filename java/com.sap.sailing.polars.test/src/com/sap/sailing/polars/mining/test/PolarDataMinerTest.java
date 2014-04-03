package com.sap.sailing.polars.mining.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sailing.polars.mining.PolarClusterKey;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.mining.RoundedAngleToTheWind;
import com.sap.sailing.polars.mining.WindSpeedLevel;
import com.sap.sse.datamining.factories.GroupKeyFactory;
import com.sap.sse.datamining.shared.GroupKey;

public class PolarDataMinerTest {

    private static final int MILLISECONDS_OVER_WHICH_TO_AVERAGE_SPEED = 30;

    @Test
    public void testGrouping() throws InterruptedException, TimeoutException, NoSuchMethodException {
        PolarDataMiner miner = new PolarDataMiner();

        GPSFixMoving fix = createMockedFix();
        Competitor competitor = mock(Competitor.class);
        TrackedRace trackedRace = createMockedTrackedRace(competitor, fix);
        miner.addFix(fix, competitor, trackedRace);
        int millisLeft = 500000;
        while (miner.isCurrentlyActiveAndOrHasQueue() && millisLeft > 0) {
            Thread.sleep(100);
            millisLeft = millisLeft - 100;
            if (miner.isCurrentlyActiveAndOrHasQueue() && millisLeft <= 0) {
                throw new TimeoutException();
            }
        }
        PolarClusterKey key = new PolarClusterKey() {

            @Override
            public WindSpeedLevel getWindSpeedLevel() {
                return new WindSpeedLevel(7, PolarSheetGenerationSettingsImpl.createStandardPolarSettings()
                        .getWindStepping());
            }

            @Override
            public RoundedAngleToTheWind getRoundedAngleToTheWind() {
                return new RoundedAngleToTheWind(-45);
            }
        };
        GroupKey compoundKey = GroupKeyFactory.createCompoundKeyFor(key, miner.getClusterKeyDimensions().iterator());
        Set<GPSFixMovingWithPolarContext> fixWithPolarContext = miner.getContainer(compoundKey);
        Assert.assertNotNull(fixWithPolarContext);
    }

    private GPSFixMoving createMockedFix() {
        GPSFixMoving fix = mock(GPSFixMoving.class);
        when(fix.getPosition()).thenReturn(new DegreePosition(54.431952, 10.186767));
        Calendar cal = Calendar.getInstance();
        cal.set(2014, 4, 3, 13, 00);
        TimePoint fixTimePoint = new MillisecondsTimePoint(cal.getTime());
        when(fix.getTimePoint()).thenReturn(fixTimePoint);
        Bearing bearing = new DegreeBearingImpl(45);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(10.5, bearing);
        when(fix.getSpeed()).thenReturn(speedWithBearing);
        return fix;
    }

    private TrackedRace createMockedTrackedRace(Competitor competitor, GPSFixMoving fix) {
        TrackedRace trackedRace = mock(TrackedRace.class);

        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = new DynamicGPSFixMovingTrackImpl<Competitor>(competitor,
                MILLISECONDS_OVER_WHICH_TO_AVERAGE_SPEED);
        track.add(fix);
        when(trackedRace.getTrack(competitor)).thenReturn(track);

        RaceDefinition mockedRaceDefinition = createMockedRaceDefinition();
        when(trackedRace.getRace()).thenReturn(mockedRaceDefinition);

        MarkPassing markpassing = createMockedStartMarkPassing();
        when(trackedRace.getMarkPassing(eq(competitor), any(Waypoint.class))).thenReturn(markpassing);

        Calendar cal = Calendar.getInstance();
        cal.set(2014, 4, 3, 12, 00);
        TimePoint startOfRace = new MillisecondsTimePoint(cal.getTime());

        cal.set(2014, 4, 3, 15, 00);
        TimePoint endOfRace = new MillisecondsTimePoint(cal.getTime());

        when(trackedRace.getStartOfRace()).thenReturn(startOfRace);
        when(trackedRace.getEndOfRace()).thenReturn(endOfRace);
        
        Bearing windBearing = new DegreeBearingImpl(0);
        SpeedWithBearing windSpeed = new KnotSpeedWithBearingImpl(15, windBearing);
        
        Wind wind = new WindImpl(fix.getPosition(), fix.getTimePoint(), windSpeed);
        when(trackedRace.getWind(fix.getPosition(), fix.getTimePoint())).thenReturn(wind);

        return trackedRace;
    }

    private MarkPassing createMockedStartMarkPassing() {
        Calendar cal = Calendar.getInstance();
        cal.set(2014, 4, 3, 12, 15);
        TimePoint startOfRaceForCompetitor = new MillisecondsTimePoint(cal.getTime());

        MarkPassing passing = mock(MarkPassing.class);
        when(passing.getTimePoint()).thenReturn(startOfRaceForCompetitor);
        return passing;
    }

    private RaceDefinition createMockedRaceDefinition() {
        RaceDefinition raceDefinition = mock(RaceDefinition.class);
        Course mockedCourse = createMockedCourse();
        when(raceDefinition.getCourse()).thenReturn(mockedCourse);
        BoatClass mockedBoatClass = mock(BoatClass.class);
        when(mockedBoatClass.getManeuverDegreeAngleThreshold()).thenReturn(20.0);
        when(raceDefinition.getBoatClass()).thenReturn(mockedBoatClass);
        return raceDefinition;
    }

    private Course createMockedCourse() {
        Course course = mock(Course.class);
        return course;
    }

}
