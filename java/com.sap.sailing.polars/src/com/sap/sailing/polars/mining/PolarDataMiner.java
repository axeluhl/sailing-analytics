package com.sap.sailing.polars.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsHistogramDataImpl;
import com.sap.sailing.domain.common.impl.WindSteppingWithMaxDistance;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionsValueNestingGroupingProcessor;

public class PolarDataMiner {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = createExecutor();
    private final PolarSheetGenerationSettings defaultPolarSheetGenerationSettings;

    private static ThreadPoolExecutor createExecutor() {
        return new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }


    private AbstractEnrichingProcessor<Triple<GPSFixMoving, TrackedRace, Competitor>, GPSFixMovingWithPolarContext> enrichingProcessor;
    private IncrementalRegressionProcessor incrementalRegressionProcessor;

    public PolarDataMiner() {
        defaultPolarSheetGenerationSettings = PolarSheetGenerationSettingsImpl.createStandardPolarSettings();
        try {
            setUpWorkflow();
        } catch (ClassCastException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    private void setUpWorkflow() throws ClassCastException, NoSuchMethodException,
            SecurityException {
        WindSteppingWithMaxDistance stepping = defaultPolarSheetGenerationSettings.getWindStepping();
        final ClusterGroup<Speed> speedClusterGroup = SpeedClusterGroupFromWindSteppingCreator
                .createSpeedClusterGroupFrom(stepping);
        incrementalRegressionProcessor = new IncrementalRegressionProcessor(speedClusterGroup);
        Collection<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, ?>> grouperResultReceivers = new ArrayList<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, ?>>();
        grouperResultReceivers.add(incrementalRegressionProcessor);

        Collection<Function<?>> dimensions = PolarDataDimensionCollectionFactory.getClusterKeyDimensions();

        Processor<GPSFixMovingWithPolarContext, GroupedDataEntry<GPSFixMovingWithPolarContext>> groupingProcessor = new ParallelMultiDimensionsValueNestingGroupingProcessor<GPSFixMovingWithPolarContext>(
                GPSFixMovingWithPolarContext.class, executor, grouperResultReceivers, dimensions);

        Collection<Processor<GPSFixMovingWithPolarContext, ?>> filteringResultReceivers = Arrays.asList(groupingProcessor);
        Processor<GPSFixMovingWithPolarContext, GPSFixMovingWithPolarContext> filteringProcessor = new ParallelFilteringProcessor<GPSFixMovingWithPolarContext>(
                GPSFixMovingWithPolarContext.class, executor, filteringResultReceivers, new PolarFixFilterCriteria());

        Collection<Processor<GPSFixMovingWithPolarContext, GPSFixMovingWithPolarContext>> enrichingResultReceivers = Arrays
                .asList(filteringProcessor);


        enrichingProcessor = new AbstractEnrichingProcessor<Triple<GPSFixMoving, TrackedRace, Competitor>, GPSFixMovingWithPolarContext>(
                executor, enrichingResultReceivers) {

            @Override
            protected GPSFixMovingWithPolarContext enrich(Triple<GPSFixMoving, TrackedRace, Competitor> element) {
                return new GPSFixMovingWithPolarContext(element.getA(), element.getB(), element.getC(),
                        speedClusterGroup);
            }
        };
    }


    public void addFix(GPSFixMoving fix, Competitor competitor, TrackedRace trackedRace) {
        enrichingProcessor.processElement(new Triple<GPSFixMoving, TrackedRace, Competitor>(fix, trackedRace,
                competitor));
    }

    public boolean isCurrentlyActiveAndOrHasQueue() {
        boolean isActive = executor.getActiveCount() > 0;
        boolean hasQueue = executor.getQueue().size() > 0;
        return isActive || hasQueue;
    }

    public SpeedWithConfidence<Integer> estimateBoatSpeed(BoatClass boatClass, Speed windSpeed, Bearing angleToTheWind)
            throws NotEnoughDataHasBeenAddedException {
        return incrementalRegressionProcessor.estimateBoatSpeed(boatClass, windSpeed, angleToTheWind).getA();
    }



    public PolarSheetsData createFullSheetForBoatClass(BoatClass boatClass) {
        Integer[] defaultWindSpeeds = defaultPolarSheetGenerationSettings.getWindStepping().getRawStepping();
        Number[][] averagedPolarDataByWindSpeed = new Number[defaultWindSpeeds.length][360];
        
        Map<Integer, Integer[]> dataCountPerAngleForWindspeed = new HashMap<>();
        Map<Integer, Map<Integer, PolarSheetsHistogramData>> histogramDataMap = new HashMap<>();
        
        int totalDataCount = 0;
        
        for (int windIndex = 0; windIndex < defaultWindSpeeds.length; windIndex++) {
            Integer windSpeed = defaultWindSpeeds[windIndex];
            
            Integer[] perAngle = new Integer[360];
            Map<Integer, PolarSheetsHistogramData> perWindSpeed = new HashMap<>();
            for (int angle = 0; angle < 360; angle++) {
                Pair<SpeedWithConfidence<Integer>, Integer> speedWithConfidenceAndDataCount;
                try {
                    int convertedAngle = angle;
                    if (angle > 180) {
                        convertedAngle = angle - 360;
                    }
                    speedWithConfidenceAndDataCount = incrementalRegressionProcessor.estimateBoatSpeed(boatClass, new KnotSpeedImpl(windSpeed), new DegreeBearingImpl(convertedAngle));
                } catch (NotEnoughDataHasBeenAddedException e) {
                    speedWithConfidenceAndDataCount = new Pair<SpeedWithConfidence<Integer>, Integer>(new SpeedWithConfidenceImpl<Integer>(new KnotSpeedImpl(0), 0, 0), 0);
                }
                            
                averagedPolarDataByWindSpeed[windIndex][angle] = speedWithConfidenceAndDataCount.getA().getObject().getKnots();
                int dataCount = speedWithConfidenceAndDataCount.getB();
                
                totalDataCount = totalDataCount + dataCount;
                //FIXME hard coded
                double coefficiantOfVariation = 0.8;
                double confidenceMeasure = 0.5;
                
                PolarSheetsHistogramDataImpl polarSheetsHistogramDataImpl = createEmptyHistogramData(perAngle, angle,
                        dataCount, coefficiantOfVariation, confidenceMeasure);
                perWindSpeed.put(angle, polarSheetsHistogramDataImpl);
            }
            histogramDataMap.put(windIndex, perWindSpeed);
            dataCountPerAngleForWindspeed.put(windIndex, perAngle);
        }
        
        
        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, totalDataCount,
                dataCountPerAngleForWindspeed, defaultPolarSheetGenerationSettings.getWindStepping(), histogramDataMap);
        return data;
    }



    private PolarSheetsHistogramDataImpl createEmptyHistogramData(Integer[] perAngle, int angle, int dataCount,
            double coefficiantOfVariation, double confidenceMeasure) {
        perAngle[angle] = dataCount;
        Number[] xValues = {};
        Number[] yValues = {};;
        Map<String, Integer[]> yValuesByGaugeIds = new HashMap<>();
        Map<String, Integer[]> yValuesByDay = new HashMap<>();
        Map<String, Integer[]> yValuesByDayAndGaugeId = new HashMap<>();
        
        
        PolarSheetsHistogramDataImpl polarSheetsHistogramDataImpl = new PolarSheetsHistogramDataImpl(angle, xValues, yValues, yValuesByGaugeIds,
                yValuesByDay, yValuesByDayAndGaugeId, dataCount, coefficiantOfVariation);
        
        polarSheetsHistogramDataImpl.setConfidenceMeasure(confidenceMeasure);
        return polarSheetsHistogramDataImpl;
    }



    public Set<BoatClass> getAvailableBoatClasses() {
        return incrementalRegressionProcessor.getAvailableBoatClasses();
    }

}
