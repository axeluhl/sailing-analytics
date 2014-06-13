package com.sap.sailing.polars.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.WindSteppingWithMaxDistance;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionalGroupingProcessor;

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
        List<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>>> grouperResultReceivers = new ArrayList<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>>>();
        grouperResultReceivers.add(incrementalRegressionProcessor);

        Collection<Function<?>> dimensions = PolarDataDimensionCollectionFactory.getClusterKeyDimensions();

        Processor<GPSFixMovingWithPolarContext> groupingProcessor = new ParallelMultiDimensionalGroupingProcessor<GPSFixMovingWithPolarContext>(
                executor, grouperResultReceivers, dimensions);

        Collection<Processor<GPSFixMovingWithPolarContext>> filteringResultReceivers = Arrays.asList(groupingProcessor);
        Processor<GPSFixMovingWithPolarContext> filteringProcessor = new ParallelFilteringProcessor<GPSFixMovingWithPolarContext>(
                executor, filteringResultReceivers, new PolarFixFilterCriteria());

        Collection<Processor<GPSFixMovingWithPolarContext>> enrichingResultReceivers = Arrays
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
        return incrementalRegressionProcessor.estimateBoatSpeed(boatClass, windSpeed, angleToTheWind);
    }

}
