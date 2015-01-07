package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.components.AbstractSimpleParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelFilteringProcessor;
import com.sap.sse.datamining.impl.criterias.AbstractFilterCriterion;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.datamining.test.util.components.BlockingProcessor;
import com.sap.sse.datamining.test.util.components.NullProcessor;
import com.sap.sse.datamining.test.util.components.Number;

public class TestProcessorQuery {
    
    private final static DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    private ProcessorFactory processorFactory;

    private boolean receivedElementOrFinished;
    private boolean receivedAbort;
    
    @Before
    public void initializeProcessorFactory() {
        processorFactory = new ProcessorFactory(ConcurrencyTestsUtil.getExecutor());
    }

    @Test
    public void testStandardWorkflow() throws InterruptedException, ExecutionException {
        Collection<Number> dataSource = createDataSource();
        Query<Double> queryWithStandardWorkflow = createQueryWithStandardWorkflow(dataSource);
        QueryResult<Double> expectedResult = buildExpectedResult(dataSource);
        verifyResult(queryWithStandardWorkflow.run(), expectedResult);
    }
    
    private Collection<Number> createDataSource() {
        Collection<Number> dataSource = new ArrayList<>();
        
        //Should be removed after filtering the data
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
        final ThreadPoolExecutor executor = ConcurrencyTestsUtil.getExecutor();
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(dataSource, stringMessages, Locale.ENGLISH) {
            @Override
            protected Processor<Iterable<Number>, ?> createFirstProcessor() {
                Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> sumAggregator = processorFactory.createAggregationProcessor(this, AggregatorType.Sum, Double.class);
                
                Method getCrossSumMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getCrossSum");
                Function<Double> getCrossSumFunction = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(getCrossSumMethod);
                Processor<GroupedDataEntry<Number>, GroupedDataEntry<Double>> crossSumExtractor = processorFactory.createExtractionProcessor(sumAggregator, getCrossSumFunction);

                List<Function<?>> dimensions = new ArrayList<>();
                Function<Integer> getLengthFunction = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(Number.class, "getLength"));
                dimensions.add(getLengthFunction);
                Processor<Number, GroupedDataEntry<Number>> lengthGrouper = processorFactory.createGroupingProcessor(Number.class, crossSumExtractor, dimensions);
                Collection<Processor<Number, ?>> filtrationResultReceivers = new ArrayList<>();
                filtrationResultReceivers.add(lengthGrouper);
                
                FilterCriterion<Number> filterCriterion = new AbstractFilterCriterion<Number>(Number.class) {
                    @Override
                    public boolean matches(Number element) {
                        return element.getValue() >= 10;
                    }
                };
                Processor<Number, Number> filtrationProcessor =  new ParallelFilteringProcessor<>(Number.class, executor, filtrationResultReceivers, filterCriterion);
                Collection<Processor<Number, ?>> retrievalResultReceivers = new ArrayList<>();
                retrievalResultReceivers.add(filtrationProcessor);
                
                @SuppressWarnings("unchecked")
                Processor<Iterable<Number>, Number> retrievalProcessor = new AbstractSimpleRetrievalProcessor<Iterable<Number>, Number>((Class<Iterable<Number>>)(Class<?>) Iterable.class, Number.class,
                                                                                                                                                          ConcurrencyTestsUtil.getExecutor(), retrievalResultReceivers) {
                    @Override
                    protected Iterable<Number> retrieveData(Iterable<Number> element) {
                        return element;
                    }
                };
                
                return retrievalProcessor;
            }
        };
        return query;
    }

    private QueryResult<Double> buildExpectedResult(Collection<Number> dataSource) {
        Map<GroupKey, Double> results = new HashMap<>();
        results.put(new GenericGroupKey<Integer>(2), 5.0);
        results.put(new GenericGroupKey<Integer>(3), 3.0);
        results.put(new GenericGroupKey<Integer>(4), 10.0);
        
        QueryResultImpl<Double> result = new QueryResultImpl<>(results, new AdditionalResultDataImpl(dataSource.size() - 2, "Cross Sum (Sum)", Unit.None, 0, 0));
        return result;
    }

    private void verifyResult(QueryResult<Double> result, QueryResult<Double> expectedResult) {
        assertThat("Result values aren't correct.", result.getResults(), is(expectedResult.getResults()));
        assertThat("Retrieved data amount isn't correct.", result.getRetrievedDataAmount(), is(expectedResult.getRetrievedDataAmount()));
        assertThat("Result signifier isn't correct.", result.getResultSignifier(), is(expectedResult.getResultSignifier()));
        assertThat("Unit isn't correct.", result.getUnit(), is(expectedResult.getUnit()));
        assertThat("Value decimals aren't correct.", result.getValueDecimals(), is(expectedResult.getValueDecimals()));
    }

    @Test//(timeout=2000)
    public void testQueryTimeouting() throws TimeoutException {
        receivedElementOrFinished = false;
        receivedAbort = false;
        
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                createDataSource(), stringMessages, Locale.ENGLISH) {
            @SuppressWarnings("unchecked")
            @Override
            protected Processor<Iterable<Number>, ?> createFirstProcessor() {
                Processor<Double, Void> resultReceiver = new NullProcessor<Double, Void>(Double.class, Void.class) {
                    @Override
                    public void processElement(Double element) {
                        receivedElementOrFinished = true;
                    }
                    @Override
                    public void finish() throws InterruptedException {
                        receivedElementOrFinished = true;
                    }
                    @Override
                    public void abort() {
                        receivedAbort = true;
                    }
                };
                Collection<Processor<Double, ?>> resultReceivers = new ArrayList<>();
                resultReceivers.add(resultReceiver);
                return new BlockingProcessor<Iterable<Number>, Double>((Class<Iterable<Number>>)(Class<?>) Iterable.class, Double.class, ConcurrencyTestsUtil.getExecutor(), resultReceivers, 2000);
            }
        };
        
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
        final String keyValue = "Sum";
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                createDataSource(), stringMessages, Locale.ENGLISH) {
            @Override
            protected Processor<Iterable<Number>, ?> createFirstProcessor() {
                return createSumBuildingProcessor(/*query*/ this, keyValue);
            }
        };
        
        Map<GroupKey, Double> expectedResult = new HashMap<>();
        expectedResult.put(new GenericGroupKey<String>(keyValue), 10358.0);
        assertThat(query.run(500, TimeUnit.MILLISECONDS).getResults(), is(expectedResult));
    }

    @SuppressWarnings("unchecked")
    private AbstractSimpleParallelProcessor<Iterable<Number>, Map<GroupKey, Double>> createSumBuildingProcessor(
            ProcessorQuery<Double, Iterable<Number>> query, final String keyValue) {
        Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers = new ArrayList<>();
        resultReceivers.add(query.getResultReceiver());
        return new AbstractSimpleParallelProcessor<Iterable<Number>, Map<GroupKey, Double>>((Class<Iterable<Number>>)(Class<?>) Iterable.class,
                                                                                            (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
                                                                                            ConcurrencyTestsUtil.getExecutor(),
                                                                                            resultReceivers) {
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
