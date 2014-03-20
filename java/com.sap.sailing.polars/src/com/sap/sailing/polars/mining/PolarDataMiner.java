package com.sap.sailing.polars.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionalGroupingProcessor;

public class PolarDataMiner {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final ThreadPoolExecutor executor = createExecutor();

    private static ThreadPoolExecutor createExecutor() {
        return new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }


    private AbstractEnrichingProcessor<Triple<GPSFixMoving, TrackedRace, Competitor>, GPSFixMovingWithPolarContext> enrichingProcessor;

    public PolarDataMiner() {
        try {
            setUpWorkflow();
        } catch (ClassCastException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    private void setUpWorkflow() throws ClassCastException, NoSuchMethodException,
            SecurityException {
        Collection<Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>>> grouperResultReceivers = null;
        Collection<Function<?>> dimensions = new ArrayList<>();
        Function<Integer> function = FunctionFactory.createMethodWrappingFunction(GPSFixMovingWithPolarContext.class
                .getMethod("roundedAngleToTheWind", new Class<?>[0]));
        dimensions.add(function);
        Processor<GPSFixMovingWithPolarContext> groupingProcessor = new ParallelMultiDimensionalGroupingProcessor<GPSFixMovingWithPolarContext>(
                executor, grouperResultReceivers, dimensions);
        Collection<Processor<GPSFixMovingWithPolarContext>> filteringResultReceivers = Arrays.asList(groupingProcessor);
        Processor<GPSFixMovingWithPolarContext> filteringProcessor = new ParallelFilteringProcessor<GPSFixMovingWithPolarContext>(
                executor, filteringResultReceivers, new PolarFixFilterCriteria());
        Collection<Processor<GPSFixMovingWithPolarContext>> enrichingResultReceivers = Arrays
                .asList(filteringProcessor);
        enrichingProcessor = new AbstractEnrichingProcessor<Triple<GPSFixMoving, TrackedRace, Competitor>, GPSFixMovingWithPolarContext>(executor, enrichingResultReceivers) {

            @Override
            protected GPSFixMovingWithPolarContext enrich(Triple<GPSFixMoving, TrackedRace, Competitor> element) {
                return new GPSFixMovingWithPolarContext(element.getA(), element.getB(), element.getC());
            }
        };
    }


    public void addFix(GPSFixMoving fix, Competitor competitor, TrackedRace trackedRace) {
        enrichingProcessor.onElement(new Triple<GPSFixMoving, TrackedRace, Competitor>(fix, trackedRace, competitor));
    }

}
