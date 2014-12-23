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
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionsValueNestingGroupingProcessor;

public class PolarDataMiner {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = createExecutor();
    private final PolarSheetGenerationSettings backendPolarSheetGenerationSettings;

    private static ThreadPoolExecutor createExecutor() {
        return new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }


    private AbstractEnrichingProcessor<GPSFixMovingWithOriginInfo, GPSFixMovingWithPolarContext> enrichingProcessor;
    private IncrementalRegressionProcessor incrementalRegressionProcessor;

    public PolarDataMiner() {
        backendPolarSheetGenerationSettings = PolarSheetGenerationSettingsImpl.createBackendPolarSettings();
        try {
            setUpWorkflow();
        } catch (ClassCastException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    private void setUpWorkflow() throws ClassCastException, NoSuchMethodException,
            SecurityException {
        WindSteppingWithMaxDistance stepping = backendPolarSheetGenerationSettings.getWindStepping();
        final ClusterGroup<Speed> speedClusterGroup = SpeedClusterGroupFromWindSteppingCreator
                .createSpeedClusterGroupFrom(stepping);
        incrementalRegressionProcessor = new IncrementalRegressionProcessor(speedClusterGroup);
        Collection<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, ?>> grouperResultReceivers = new ArrayList<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, ?>>();
        grouperResultReceivers.add(incrementalRegressionProcessor);

        Collection<Pair<Function<?>, ParameterProvider>> dimensionsWithParameterProviders = new ArrayList<>();
        for (Function<?> function : PolarDataDimensionCollectionFactory.getClusterKeyDimensions()) {
            dimensionsWithParameterProviders.add(new Pair<Function<?>, ParameterProvider>(function, ParameterProvider.NULL));
        }

        Processor<GPSFixMovingWithPolarContext, GroupedDataEntry<GPSFixMovingWithPolarContext>> groupingProcessor = new ParallelMultiDimensionsValueNestingGroupingProcessor<GPSFixMovingWithPolarContext>(
                GPSFixMovingWithPolarContext.class, executor, grouperResultReceivers, dimensionsWithParameterProviders);

        Collection<Processor<GPSFixMovingWithPolarContext, ?>> filteringResultReceivers = Arrays.asList(groupingProcessor);
        Processor<GPSFixMovingWithPolarContext, GPSFixMovingWithPolarContext> filteringProcessor = new ParallelFilteringProcessor<GPSFixMovingWithPolarContext>(
                GPSFixMovingWithPolarContext.class, executor, filteringResultReceivers, new PolarFixFilterCriteria());

        Collection<Processor<GPSFixMovingWithPolarContext, ?>> enrichingResultReceivers = Arrays
                .asList(filteringProcessor);


        enrichingProcessor = new AbstractEnrichingProcessor<GPSFixMovingWithOriginInfo, GPSFixMovingWithPolarContext>(
                GPSFixMovingWithOriginInfo.class, GPSFixMovingWithPolarContext.class, executor, enrichingResultReceivers) {

            @Override
            protected GPSFixMovingWithPolarContext enrich(GPSFixMovingWithOriginInfo element) {
                return new GPSFixMovingWithPolarContext(element.getFix(), element.getTrackedRace(), element.getCompetitor(),
                        speedClusterGroup);
            }
        };
    }


    public void addFix(GPSFixMoving fix, Competitor competitor, TrackedRace trackedRace) {
        enrichingProcessor.processElement(new GPSFixMovingWithOriginInfo(fix, trackedRace,
                competitor));
    }

    public boolean isCurrentlyActiveAndOrHasQueue() {
        boolean isActive = executor.getActiveCount() > 0;
        boolean hasQueue = executor.getQueue().size() > 0;
        return isActive || hasQueue;
    }

    /**
     * 
     * @param boatClass
     * @param windSpeed
     * @param angleToTheWind
     * @param useLinearRegression if true uses lin. regression in the wind interval, otherwise arithm. mean
     * @return
     * @throws NotEnoughDataHasBeenAddedException
     */
    public SpeedWithConfidence<Void> estimateBoatSpeed(BoatClass boatClass, Speed windSpeed, Bearing angleToTheWind,
            boolean useLinearRegression)
            throws NotEnoughDataHasBeenAddedException {
        return incrementalRegressionProcessor.estimateBoatSpeed(boatClass, windSpeed, angleToTheWind, useLinearRegression).getA();
    }



    public PolarSheetsData createFullSheetForBoatClass(BoatClass boatClass) {
        Double[] defaultWindSpeeds = backendPolarSheetGenerationSettings.getWindStepping().getRawStepping();
        Number[][] averagedPolarDataByWindSpeed = new Number[defaultWindSpeeds.length][360];
        
        Map<Integer, Integer[]> dataCountPerAngleForWindspeed = new HashMap<>();
        Map<Integer, Map<Integer, PolarSheetsHistogramData>> histogramDataMap = new HashMap<>();
        
        int totalDataCount = 0;
        
        for (int windIndex = 0; windIndex < defaultWindSpeeds.length; windIndex++) {
            Double windSpeed = defaultWindSpeeds[windIndex];
            
            Integer[] perAngle = new Integer[360];
            Map<Integer, PolarSheetsHistogramData> perWindSpeed = new HashMap<>();
            for (int angle = 0; angle < 360; angle++) {
                Pair<SpeedWithConfidence<Void>, Integer> speedWithConfidenceAndDataCount;
                try {
                    int convertedAngle = convertAngleIfNecessary(angle);
                    speedWithConfidenceAndDataCount = incrementalRegressionProcessor.estimateBoatSpeed(boatClass,
                            new KnotSpeedImpl(windSpeed), new DegreeBearingImpl(convertedAngle), true);
                } catch (NotEnoughDataHasBeenAddedException e) {
                    // No data so put in a 0 speed with 0 confidence
                    speedWithConfidenceAndDataCount = new Pair<SpeedWithConfidence<Void>, Integer>(
                            new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(0), 0, null), 0);
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
                dataCountPerAngleForWindspeed, backendPolarSheetGenerationSettings.getWindStepping(), histogramDataMap);
        return data;
    }



    private int convertAngleIfNecessary(int angle) {
        int convertedAngle = angle;
        if (angle > 180) {
            convertedAngle = angle - 360;
        }
        return convertedAngle;
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



    public Integer[] getDataCountsForWindSpeed(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive) {
        Integer[] dataCounts = new Integer[360];
        for (int angle = 0; angle < 360; angle++) {
            if (angle >= startAngleInclusive && angle < endAngleExclusive) {
                try {
                    dataCounts[angle] = incrementalRegressionProcessor.estimateBoatSpeed(boatClass, windSpeed,
                            new DegreeBearingImpl(convertAngleIfNecessary(angle)), true).getB();
                } catch (NotEnoughDataHasBeenAddedException e) {
                    dataCounts[angle] = 0;
                }
            } else {
                dataCounts[angle] = null;
            }
        }
        return dataCounts;
    }

}
