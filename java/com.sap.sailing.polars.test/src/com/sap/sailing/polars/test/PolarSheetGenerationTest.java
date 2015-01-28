package com.sap.sailing.polars.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingByTimeComparator;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.polars.aggregation.PolarFixAggregationWorker;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.aggregation.SimplePolarFixRaceInterval;
import com.sap.sailing.polars.data.DataPointWithOriginInfo;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.data.impl.DataPointWithOriginInfoImpl;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.polars.generation.PolarSheetHistogramBuilder;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class PolarSheetGenerationTest {
    
    @Test
    public void testPolarSheetRawDataGeneration() throws InterruptedException, ExecutionException {
        Executor executor = new ThreadPoolExecutor(/* corePoolSize */Runtime.getRuntime().availableProcessors(),
        /* maximumPoolSize */Runtime.getRuntime().availableProcessors(),
        /* keepAliveTime */60, TimeUnit.SECONDS,
        /* workQueue */new LinkedBlockingQueue<Runnable>());

        MockTrackedRaceForPolarSheetGeneration race = new MockTrackedRaceForPolarSheetGeneration();
        
        double[] levels = { 4., 6., 8., 10., 12., 14., 16., 20., 25., 30. };
        WindSpeedSteppingWithMaxDistance windStepping = new WindSpeedSteppingWithMaxDistance(levels, 2.0);
        PolarSheetGenerationSettings settings = new PolarSheetGenerationSettingsImpl(1, 0, 1, 20, 0, false, true, 5,
                0.05, false, windStepping, false);
        
        TimePoint startTime = new MillisecondsTimePoint(9);
        TimePoint endTime = new MillisecondsTimePoint(80);
        // Only used for storing and exporting results in this test case:
        PolarFixAggregator resultContainer = new PolarFixAggregator(new SimplePolarFixRaceInterval(
                new HashSet<TrackedRace>()),
                settings, executor);

        BoatClass forelle = new BoatClassImpl("Forelle", true);
        Competitor competitor = new CompetitorImpl(UUID.randomUUID(), "Hans Frantz", Color.RED, new TeamImpl("SAP", null, null),
                new BoatImpl("Schnelle Forelle", forelle, "GER000"));

        PolarFixAggregationWorker task = new PolarFixAggregationWorker(race,
 resultContainer, startTime, endTime,
                competitor, settings, null);
  
        executor.execute(task);
        double timeUntilTimeout = 1000;
        while (!task.isDone() && timeUntilTimeout > 0) {
            Thread.sleep(100);
            timeUntilTimeout = timeUntilTimeout - 0.1;
        }

        Set<PolarFix> fixes = resultContainer.getAggregationResultAsSingleList();
        
        PolarSheetGenerator generator = new PolarSheetGenerator(fixes, settings);
        PolarSheetsData data = generator.generate();
        Assert.assertEquals(4, data.getDataCount());
        Assert.assertEquals(4.0, data.getAveragedPolarDataByWindSpeed()[0][45]);
        Assert.assertEquals(2.0, data.getAveragedPolarDataByWindSpeed()[0][55]);
        Assert.assertEquals(6.0, data.getAveragedPolarDataByWindSpeed()[0][35]);
        
    }
    
    @Test
    public void testHistogramBuilder() {
        double[] levels = { 4., 6., 8., 10., 12., 14., 16., 20., 25., 30. };
        WindSpeedSteppingWithMaxDistance windStepping = new WindSpeedSteppingWithMaxDistance(levels, 2.0);
        PolarSheetGenerationSettings settings = new PolarSheetGenerationSettingsImpl(1, 0, 1, 10, 0, false, true, 5,
                0.05, false, windStepping, false);
        PolarSheetHistogramBuilder builder = new PolarSheetHistogramBuilder(settings);
        
        
        List<DataPointWithOriginInfo> rawData = new ArrayList<DataPointWithOriginInfo>();
        rawData.add(new DataPointWithOriginInfoImpl(1.09, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(1.0, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(1.11, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(1.46, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(1.56, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(2.05, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(2.09, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(3.0, "", ""));
        rawData.add(new DataPointWithOriginInfoImpl(2.999, "", ""));
        PolarSheetsHistogramData result = builder.build(rawData, 0, 0);
        Number[] xValues = result.getxValues();
        Assert.assertEquals(10, xValues.length);
        Assert.assertTrue(xValues[4].doubleValue() > 1.9 - 0.001 && xValues[4].doubleValue() < 1.9 + 0.001);
        Number[] yValues = result.getyValues();
        Assert.assertEquals(10, yValues.length);
        Assert.assertEquals(3, yValues[0]);
        Assert.assertEquals(0, yValues[1]);
        Assert.assertEquals(2, yValues[2]);
        Assert.assertEquals(2, yValues[9]);
    }
    
    @Test
    public void testOutlierNeighborhoodAlgorithm() {
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        List<DataPointWithOriginInfo> values = new ArrayList<DataPointWithOriginInfo>();
        values.add(new DataPointWithOriginInfoImpl(0., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        values.add(new DataPointWithOriginInfoImpl(20., "", ""));
        int[] count = new int[1];
        count[0] = values.size();
        Map<Integer, List<DataPointWithOriginInfo>> valuesInMap = new HashMap<Integer, List<DataPointWithOriginInfo>>();
        valuesInMap.put(0, values);
        double pct = PolarSheetGenerator.getNeighboorhoodSizePercentage(count, valuesInMap, 0, 0,
                new DataPointWithOriginInfoImpl(0.0, "", ""), settings);

        Assert.assertTrue(pct < 0.05);

        pct = PolarSheetGenerator.getNeighboorhoodSizePercentage(count, valuesInMap, 0, 2,
                new DataPointWithOriginInfoImpl(20.0, "", ""), settings);
  
        Assert.assertTrue(pct > 0.05);
        
    }
    
    
    @SuppressWarnings("serial")
    private class MockTrackedRaceForPolarSheetGeneration extends MockedTrackedRace {
        @Override
        public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = new MockDynamicGPSFixMovinTrackForPolarSheetGeneration<Competitor>(competitor, 0);
            //Before start
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(0.009, 0.009), new MillisecondsTimePoint(3), new KnotSpeedWithBearingImpl(12.0, new DegreeBearingImpl(-45.0))));
            //Race
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.0, 1.0), new MillisecondsTimePoint(10), new KnotSpeedWithBearingImpl(3.0, new DegreeBearingImpl(-45.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.001, 1.001), new MillisecondsTimePoint(11), new KnotSpeedWithBearingImpl(5.0, new DegreeBearingImpl(-45.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.002, 1.002), new MillisecondsTimePoint(12), new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(-55.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.003, 1.003), new MillisecondsTimePoint(13), new KnotSpeedWithBearingImpl(6.0, new DegreeBearingImpl(-35.0))));
            //After Finish
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.01, 1.01), new MillisecondsTimePoint(75), new KnotSpeedWithBearingImpl(12.0, new DegreeBearingImpl(-45.0))));
            return track;
        }
        
        @Override
        public Wind getWind(Position p, TimePoint at) {
            Wind wind = new WindImpl(p, at, new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(180.0)));
            return wind;
        }
        
        @Override
        public Wind getWind(Position p, TimePoint at, Set<WindSource> windSourcesToExclude) {
            return getWind(p, at);
        }
        
        @Override
        public WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at) {
            return new WindWithConfidenceImpl<com.sap.sse.common.Util.Pair<Position, TimePoint>>(new WindImpl(p, at,
                    new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(180.0))), 0.9,
                    new com.sap.sse.common.Util.Pair<Position, TimePoint>(p, at), true);
        }
        
        @Override
        public WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p, TimePoint at,
                Set<WindSource> windSourcesToExclude) {
            return getWindWithConfidence(p, at);
        }
        
        @Override
        public RaceDefinition getRace() {
            BoatClass forelle = new BoatClassImpl("Forelle", true);
            ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
            waypoints.add(new WaypointImpl(null));
            waypoints.add(new WaypointImpl(null));
            waypoints.add(new WaypointImpl(null));
            waypoints.add(new WaypointImpl(null));
            waypoints.add(new WaypointImpl(null));
            waypoints.add(new WaypointImpl(null));
            waypoints.add(new WaypointImpl(null));
            RaceDefinition race = new RaceDefinitionImpl("Forelle1", new CourseImpl("ForelleCourse", waypoints), forelle, new ArrayList<Competitor>());
            return race;
        }
        
        @Override
        public NavigableSet<MarkPassing> getMarkPassings(Competitor competitor) {
            NavigableSet<MarkPassing> passings = new ConcurrentSkipListSet<MarkPassing>(
                    MarkPassingByTimeComparator.INSTANCE);
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(10), null, competitor));
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(20), null, competitor));
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(30), null, competitor));
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(40), null, competitor));
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(50), null, competitor));
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(60), null, competitor));
            passings.add(new MarkPassingImpl(new MillisecondsTimePoint(70), null, competitor));
            return passings;
        }
        
        @Override
        public Set<WindSource> getWindSources(WindSourceType type) {
            Set<WindSource> sources = new HashSet<>();
            sources.add(new WindSourceImpl(type));
            return sources;
        }
    }
    
    @SuppressWarnings("serial")
    private class MockGPSFixMovingForPolarSheetGeneration extends GPSFixMovingImpl {

        public MockGPSFixMovingForPolarSheetGeneration(Position position, TimePoint timePoint, SpeedWithBearing speed) {
            super(position, timePoint, speed);
        }
        
        @Override
        public boolean isValidCached() {
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
        
        @Override
        public SpeedWithBearing getEstimatedSpeed(TimePoint at) {
            return this.getFirstFixAtOrAfter(at).getSpeed();
        }
        
    }

}
