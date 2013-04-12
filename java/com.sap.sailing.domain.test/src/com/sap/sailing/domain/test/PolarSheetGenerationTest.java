package com.sap.sailing.domain.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.polarsheets.PerRaceAndCompetitorPolarSheetGenerationWorker;
import com.sap.sailing.domain.polarsheets.PolarSheetGenerationWorker;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;

public class PolarSheetGenerationTest {
    
    @Test
    public void testPolarSheetRawDataGeneration() throws InterruptedException {
        Executor executor = new ThreadPoolExecutor(/* corePoolSize */ 0,
                /* maximumPoolSize */ Runtime.getRuntime().availableProcessors(),
                /* keepAliveTime */ 60, TimeUnit.SECONDS,
                /* workQueue */ new LinkedBlockingQueue<Runnable>());
        
        MockTrackedRaceForPolarSheetGeneration race = new MockTrackedRaceForPolarSheetGeneration();
        
        TimePoint startTime = new MillisecondsTimePoint(1);
        TimePoint endTime = new MillisecondsTimePoint(4);
        //Only used for storing and exporting results in this test case:
        PolarSheetGenerationWorker resultContainer = new PolarSheetGenerationWorker(new HashSet<TrackedRace>(), executor);
        BoatClass forelle = new BoatClassImpl("Forelle", true);
        Competitor competitor = new CompetitorImpl(UUID.randomUUID(), "Hans Frantz", new TeamImpl("SAP", null, null), new BoatImpl("Schnelle Forelle", forelle, "GER000"));
        PerRaceAndCompetitorPolarSheetGenerationWorker task = new PerRaceAndCompetitorPolarSheetGenerationWorker(race, resultContainer, startTime, endTime, competitor);
        
        executor.execute(task);
        
        double timeUntilTimeout = 1000;
        while (!task.isDone() && timeUntilTimeout > 0) {
            Thread.sleep(100);
            timeUntilTimeout = timeUntilTimeout - 0.1;
        }
        
        Assert.assertTrue(task.isDone());
        
        PolarSheetsData data = resultContainer.getPolarData();
        Assert.assertEquals(4, data.getDataCount());
        Assert.assertEquals(4.0, data.getAveragedPolarDataByWindSpeed()[1][45]);
        Assert.assertEquals(2.0, data.getAveragedPolarDataByWindSpeed()[1][55]);
        Assert.assertEquals(6.0, data.getAveragedPolarDataByWindSpeed()[1][30]);
        
    }
    
    
    @SuppressWarnings("serial")
    private class MockTrackedRaceForPolarSheetGeneration extends MockedTrackedRace {
        @Override
        public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = new MockDynamicGPSFixMovinTrackForPolarSheetGeneration<Competitor>(competitor, 0);
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.0, 1.0), new MillisecondsTimePoint(1), new KnotSpeedWithBearingImpl(3.0, new DegreeBearingImpl(-45.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.001, 1.001), new MillisecondsTimePoint(2), new KnotSpeedWithBearingImpl(5.0, new DegreeBearingImpl(-45.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.002, 1.002), new MillisecondsTimePoint(3), new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(-55.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.003, 1.003), new MillisecondsTimePoint(4), new KnotSpeedWithBearingImpl(6.0, new DegreeBearingImpl(-30.0))));
            return track;
        }
        
        @Override
        public Wind getWind(Position p, TimePoint at) {
            Wind wind = new WindImpl(p, at, new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(180.0)));
            return wind;
        }
        
        @Override
        public RaceDefinition getRace() {
            BoatClass forelle = new BoatClassImpl("Forelle", true);
            RaceDefinition race = new RaceDefinitionImpl("Forelle1", new CourseImpl("ForelleCourse", new ArrayList<Waypoint>()), forelle, new ArrayList<Competitor>());
            return race;
        }
    }
    
    @SuppressWarnings("serial")
    private class MockGPSFixMovingForPolarSheetGeneration extends GPSFixMovingImpl {

        public MockGPSFixMovingForPolarSheetGeneration(Position position, TimePoint timePoint, SpeedWithBearing speed) {
            super(position, timePoint, speed);
        }
        
        @Override
        public boolean isValid() {
            return true;
        }
        
    }
    
    @SuppressWarnings("serial")
    private class MockDynamicGPSFixMovinTrackForPolarSheetGeneration<ItemType> extends DynamicGPSFixMovingTrackImpl<ItemType> {

        public MockDynamicGPSFixMovinTrackForPolarSheetGeneration(ItemType trackedItem,
                long millisecondsOverWhichToAverage) {
            super(trackedItem, millisecondsOverWhichToAverage);
        }
        
        @Override
        protected boolean isValid(NavigableSet<GPSFixMoving> rawFixes, GPSFixMoving e) {
            return true;
        }
        
    }

}
