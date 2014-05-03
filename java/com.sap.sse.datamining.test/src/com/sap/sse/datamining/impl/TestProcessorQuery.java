package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.components.AbstractSimpleFilteringRetrievalProcessor;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.impl.components.ParallelMultiDimensionalGroupingProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDoubleDataSumAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;
import com.sap.sse.datamining.test.components.util.BlockingProcessor;
import com.sap.sse.datamining.test.components.util.Number;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestProcessorQuery {
    
    private final static DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();

    private boolean receivedElementOrFinished;
    private boolean receivedAbort;

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
     * Creates a query, that takes a Collection of Numbers, filters all numbers < 10, groups them by
     * their length, extracts the cross sum and aggregates these as sum.
     */
    private Query<Double> createQueryWithStandardWorkflow(Collection<Number> dataSource) {
        ThreadPoolExecutor executor = ConcurrencyTestsUtil.getExecutor();
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(executor,
                dataSource, stringMessages, Locale.ENGLISH);
        
        Collection<Processor<Map<GroupKey, Double>>> aggregationResultReceivers = Arrays.asList(query.getResultReceiver());
        Processor<GroupedDataEntry<Double>> sumAggregator =
                new ParallelGroupedDoubleDataSumAggregationProcessor(executor, aggregationResultReceivers);
        
        Method getCrossSumMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getCrossSum");
        Function<Double> getCrossSumFunction = FunctionFactory.createMethodWrappingFunction(getCrossSumMethod);
        Processor<GroupedDataEntry<Number>> crossSumExtractor = new ParallelGroupedElementsValueExtractionProcessor<Number, Double>(
                executor, Arrays.asList(sumAggregator), getCrossSumFunction);

        Collection<Function<?>> dimensions = new ArrayList<>();
        Function<Integer> getLengthFunction = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(Number.class, "getLength"));
        dimensions.add(getLengthFunction);
        Processor<Number> lengthGrouper = new ParallelMultiDimensionalGroupingProcessor<>(executor, Arrays.asList(crossSumExtractor), dimensions);
        
        FilterCriteria<Number> retrievalFilterCriteria = new FilterCriteria<Number>() {
            @Override
            public boolean matches(Number element) {
                return element.getValue() >= 10;
            }
        };
        Processor<Iterable<Number>> filteringRetrievalProcessor = new AbstractSimpleFilteringRetrievalProcessor<Iterable<Number>, Number>(ConcurrencyTestsUtil.getExecutor(), Arrays.asList(lengthGrouper), retrievalFilterCriteria) {
            @Override
            protected Iterable<Number> retrieveData(Iterable<Number> element) {
                return element;
            }
        };
        
        query.setFirstProcessor(filteringRetrievalProcessor);
        return query;
    }

    private QueryResult<Double> buildExpectedResult(Collection<Number> dataSource) {
        Map<GroupKey, Double> results = new HashMap<>();
        results.put(new GenericGroupKey<Integer>(2), 5.0);
        results.put(new GenericGroupKey<Integer>(3), 3.0);
        results.put(new GenericGroupKey<Integer>(4), 10.0);
        
        QueryResultImpl<Double> result = new QueryResultImpl<>(results, new AdditionalResultDataImpl(dataSource.size(), 18, "Cross Sum (Sum)", Unit.None, 0, 0));
        return result;
    }

    private void verifyResult(QueryResult<Double> result, QueryResult<Double> expectedResult) {
        assertThat("Result values aren't correct.", result.getResults(), is(expectedResult.getResults()));
        assertThat("Retrieved data amount isn't correct.", result.getRetrievedDataAmount(), is(expectedResult.getRetrievedDataAmount()));
        assertThat("Filtered data amount isn't correct.", result.getFilteredDataAmount(), is(expectedResult.getFilteredDataAmount()));
        assertThat("Result signifier isn't correct.", result.getResultSignifier(), is(expectedResult.getResultSignifier()));
        assertThat("Unit isn't correct.", result.getUnit(), is(expectedResult.getUnit()));
        assertThat("Value decimals aren't correct.", result.getValueDecimals(), is(expectedResult.getValueDecimals()));
    }

    @Test(timeout=2000)
    public void testQueryTimeouting() throws TimeoutException {
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                ConcurrencyTestsUtil.getExecutor(), createDataSource(), stringMessages, Locale.ENGLISH);
        Processor<Double> resultReceiver = new Processor<Double>() {
            @Override
            public void onElement(Double element) {
                receivedElementOrFinished = true;
            }
            @Override
            public void onFailure(Throwable failure) {
            }
            @Override
            public void finish() throws InterruptedException {
                receivedElementOrFinished = true;
            }
            @Override
            public void abort() {
                receivedAbort = true;
            }
            @Override
            public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
                return additionalDataBuilder;
            }
        };
        query.setFirstProcessor(new BlockingProcessor<Iterable<Number>, Double>(ConcurrencyTestsUtil.getExecutor(), Arrays.asList(resultReceiver), (long) 1000));
        
        try {
            query.run(500, TimeUnit.MILLISECONDS);
            fail("The previous line should throw a timeout exception");
        } catch (TimeoutException e) {
            // A timeout exception is expected
        }
        
        ConcurrencyTestsUtil.sleepFor(1000); // Wait if a result is received
        assertThat("The processing should be aborted", receivedElementOrFinished, is(false));
        assertThat("The processing should be aborted", receivedAbort, is(true));
    }

    @Test
    public void testQueryWithTimeoutAndNonBlockingProcess() throws TimeoutException {
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                ConcurrencyTestsUtil.getExecutor(), createDataSource(), stringMessages, Locale.ENGLISH);
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

            @Override
            protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
            }
        };
    }

}
