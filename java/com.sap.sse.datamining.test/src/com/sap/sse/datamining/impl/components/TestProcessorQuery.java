package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataSumAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;
import com.sap.sse.datamining.test.components.util.Number;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestProcessorQuery {

    @SuppressWarnings("unused")
    private boolean receivedElementOrFinished;

    @Test
    public void testStandardWorkflow() throws InterruptedException, ExecutionException {
        Collection<Number> dataSource = createDataSource();
        Query<Double> queryWithStandardWorkflow = createQueryWithStandardWorkflow(dataSource);
        QueryResult<Double> expectedResult = buildExpectedResult(dataSource);
        verifyResult(queryWithStandardWorkflow.run(), expectedResult);
    }
    
    private Collection<Number> createDataSource() {
        Collection<Number> dataSource = new ArrayList<>();
        
        //Results in <1> = 8
        dataSource.add(new Number(1));
        dataSource.add(new Number(7));

        //Results in <2> = 5
        dataSource.add(new Number(10));
        dataSource.add(new Number(10));
        dataSource.add(new Number(10));
        dataSource.add(new Number(10));
        dataSource.add(new Number(10));

        //Results in <3> = 3
        dataSource.add(new Number(100));
        dataSource.add(new Number(100));
        dataSource.add(new Number(100));

        //Results in <4> = 10
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        dataSource.add(new Number(1000));
        
        return dataSource;
    }

    /**
     * Creates a query, that takes a Collection of Numbers, groups them by
     * their length, extracts the cross sum and aggregates these as sum.
     */
    private Query<Double> createQueryWithStandardWorkflow(Collection<Number> dataSource) {
        ThreadPoolExecutor executor = ConcurrencyTestsUtil.getExecutor();
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(executor, dataSource);
        
        Collection<Processor<Map<GroupKey, Double>>> aggregationResultReceivers = asCollection(query.getResultReceiver());
        Processor<GroupedDataEntry<Double>> sumAggregator =
                new ParallelGroupedDoubleDataSumAggregationProcessor(executor, aggregationResultReceivers);
        
        Collection<Processor<GroupedDataEntry<Double>>> extractionResultReceivers = asCollection(sumAggregator);
        Method getCrossSumMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getCrossSum");
        Function<Double> getCrossSumFunction = FunctionFactory.createMethodWrappingFunction(getCrossSumMethod);
        Processor<GroupedDataEntry<Number>> crossSumExtractor = new ParallelGroupedElementsValueExtractionProcessor<Number, Double>(
                executor, extractionResultReceivers, getCrossSumFunction);
        
        Collection<Processor<GroupedDataEntry<Number>>> groupingResultReceivers = asCollection(crossSumExtractor);
        Collection<Function<?>> dimensions = new ArrayList<>();
        Function<Integer> getLengthFunction = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(Number.class, "getLength"));
        dimensions.add(getLengthFunction);
        Processor<Iterable<Number>> lengthGrouper = new ParallelMultiDimensionalGroupingProcessor<>(executor, groupingResultReceivers, dimensions);
        
        query.setFirstProcessor(lengthGrouper);
        return query;
    }

    private <T> Collection<T> asCollection(T value) {
        Collection<T> collection = new ArrayList<>();
        collection.add(value);
        return collection;
    }

    private QueryResult<Double> buildExpectedResult(Collection<Number> dataSource) {
        QueryResultImpl<Double> result = new QueryResultImpl<>(dataSource.size(), 2, "Cross sum (Sum)", Unit.None, 0);
        result.addResult(new GenericGroupKey<Integer>(1), 8.0);
        result.addResult(new GenericGroupKey<Integer>(2), 5.0);
        result.addResult(new GenericGroupKey<Integer>(3), 3.0);
        result.addResult(new GenericGroupKey<Integer>(4), 10.0);
        return result;
    }

    private void verifyResult(QueryResult<Double> result, QueryResult<Double> expectedResult) {
        assertThat("Result values aren't correct.", result.getResults(), is(expectedResult.getResults()));
//        assertThat("Retrieved data amount isn't correct.", result.getRetrievedDataAmount(), is(expectedResult.getRetrievedDataAmount()));
//        assertThat("Filtered data amount isn't correct.", result.getFilteredDataAmount(), is(expectedResult.getFilteredDataAmount()));
//        assertThat("Result signifier isn't correct.", result.getResultSignifier(), is(expectedResult.getResultSignifier()));
//        assertThat("Unit isn't correct.", result.getUnit(), is(expectedResult.getUnit()));
//        assertThat("Value decimals aren't correct.", result.getValueDecimals(), is(expectedResult.getValueDecimals()));
    }

    @Test(timeout=2000)
    public void testQueryTimeouting() throws TimeoutException {
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(ConcurrencyTestsUtil.getExecutor(), createDataSource());
        Processor<Double> resultReceiver = new Processor<Double>() {
            @Override
            public void onElement(Double element) {
                receivedElementOrFinished = true;
            }
            @Override
            public void finish() throws InterruptedException {
                receivedElementOrFinished = true;
            }
        };
        query.setFirstProcessor(createBlockingProcessor(1000, resultReceiver));
        
        try {
            query.run(500, TimeUnit.MILLISECONDS);
            fail("The previous line should throw a timeout exception");
        } catch (TimeoutException e) {
            // A timeout exception is expected
        }
        
//        ConcurrencyTestsUtil.sleepFor(1000); // Wait if a result is received
//        assertThat("The processing should be aborted", receivedElementOrFinished, is(false));
    }

    private Processor<Iterable<Number>> createBlockingProcessor(final long timeToBlockInMillis, Processor<Double> resultReceiver) {
        return new AbstractSimpleParallelProcessor<Iterable<Number>, Double>(ConcurrencyTestsUtil.getExecutor(), Arrays.asList(resultReceiver)) {
            @Override
            protected Callable<Double> createInstruction(Iterable<Number> element) {
                return new Callable<Double>() {
                    @Override
                    public Double call() throws Exception {
                        Thread.sleep(timeToBlockInMillis);
                        return 0.0;
                    }
                };
            }
        };
    }
    
    @Test
    public void testQueryWithTimeoutAndNonBlockingProcess() throws TimeoutException {
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(ConcurrencyTestsUtil.getExecutor(), createDataSource());
        String keyValue = "Sum";
        query.setFirstProcessor(createSumBuildingProcessor(query, keyValue));
        
        Map<GroupKey, Double> expectedResult = new HashMap<>();
        expectedResult.put(new GenericGroupKey<String>(keyValue), 10358.0);
        assertThat(query.run(500, TimeUnit.MILLISECONDS).getResults(), is(expectedResult));
    }

    private AbstractSimpleParallelProcessor<Iterable<Number>, Map<GroupKey, Double>> createSumBuildingProcessor(
            ProcessorQuery<Double, Iterable<Number>> query, final String keyValue) {
        return new AbstractSimpleParallelProcessor<Iterable<Number>, Map<GroupKey, Double>>(ConcurrencyTestsUtil.getExecutor(),
                                                                                            Arrays.asList(query.getResultReceiver())) {
            @Override
            protected Callable<Map<GroupKey, Double>> createInstruction(final Iterable<Number> element) {
                return new Callable<Map<GroupKey,Double>>() {
                    @Override
                    public Map<GroupKey, Double> call() throws Exception {
                        Map<GroupKey, Double> result = new HashMap<>();
                        double sum = 0;
                        for (Number number : element) {
                            sum += number.getValue();
                        }
                        result.put(new GenericGroupKey<String>(keyValue), sum);
                        return result;
                    }
                };
            }
        };
    }

}
