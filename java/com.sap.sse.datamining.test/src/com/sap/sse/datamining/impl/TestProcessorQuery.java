package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.AbstractParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.data.Unit;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.datamining.test.util.components.BlockingProcessor;
import com.sap.sse.datamining.test.util.components.NullProcessor;
import com.sap.sse.datamining.test.util.components.Number;
import com.sap.sse.datamining.test.util.components.SingleDataRetrieverChainDefinition;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class TestProcessorQuery {
    
    private static final Logger LOGGER = Logger.getLogger(TestProcessorQuery.class.getSimpleName());
    private static final ResourceBundleStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();

    private boolean receivedElementOrFinished;
    private boolean receivedAbort;
    
    private QueryResult<?> resultAfterAbortion;
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testStandardWorkflow() throws InterruptedException, ExecutionException {
        final Collection<Number> dataSource = createDataSource();
        
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        server.addStringMessages(stringMessages);
        server.setDataSourceProvider(new AbstractDataSourceProvider<Collection>(Collection.class) {
            @Override
            public Collection<?> getDataSource() {
                return dataSource;
            }
        });

        Query<Double> queryWithStandardWorkflow = server.createQuery(createQueryDefinition());
        assertThat(queryWithStandardWorkflow.getState(), is(QueryState.NOT_STARTED));
        QueryResult<Double> expectedResult = buildExpectedResult(dataSource);
        verifyResult(queryWithStandardWorkflow.run(), expectedResult);
    }

    /**
     * Creates a query definition, that filters all numbers < 10, groups them by
     * their length, extracts the cross sum and aggregates their sum.
     */
    private QueryDefinition<Collection<Number>, Number, Double> createQueryDefinition() {
        FunctionFactory functionFactory = FunctionTestsUtil.getFunctionFactory();
        
        @SuppressWarnings("unchecked")
        DataRetrieverChainDefinition<Collection<Number>, Number> retrieverChain = new SingleDataRetrieverChainDefinition<>((Class<Collection<Number>>)(Class<?>) Collection.class, Number.class, "Number");
        retrieverChain.startWith(NumberRetrievalProcessor.class, Number.class, "Number");
        
        Method getCrossSumMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getCrossSum");
        ModifiableQueryDefinition<Collection<Number>, Number, Double> definition =
                new ModifiableQueryDefinition<>(Locale.ENGLISH, retrieverChain, functionFactory.createMethodWrappingFunction(getCrossSumMethod), AggregatorType.Sum);
        
        Method getLengthMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getLength");
        definition.addDimensionToGroupBy(functionFactory.createMethodWrappingFunction(getLengthMethod));
        
        Method getValueMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getValue");
        definition.setFilterSelection(0, functionFactory.createMethodWrappingFunction(getValueMethod), Arrays.asList(10, 100, 1000));
        
        return definition;
    }
    
    private static class NumberRetrievalProcessor extends AbstractRetrievalProcessor<Collection<Number>, Number> {

        @SuppressWarnings("unchecked")
        public NumberRetrievalProcessor(ExecutorService executor, Collection<Processor<Number, ?>> resultReceivers, int retrievalLevel) {
            super((Class<Collection<Number>>)(Class<?>) Collection.class, Number.class, executor, resultReceivers, retrievalLevel);
        }

        @Override
        protected Iterable<Number> retrieveData(Collection<Number> element) {
            return element;
        }
        
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

    private QueryResult<Double> buildExpectedResult(Collection<Number> dataSource) {
        Map<GroupKey, Double> results = new HashMap<>();
        results.put(new GenericGroupKey<Integer>(2), 5.0);
        results.put(new GenericGroupKey<Integer>(3), 3.0);
        results.put(new GenericGroupKey<Integer>(4), 10.0);
        
        QueryResultImpl<Double> result = new QueryResultImpl<>(QueryResultState.NORMAL, results, new AdditionalResultDataImpl(dataSource.size() - 2, "Cross Sum (Sum)", Unit.None, "", 0, 0));
        return result;
    }

    private void verifyResult(QueryResult<Double> result, QueryResult<Double> expectedResult) {
        assertThat("The result State isn't correct.", result.getState(), is(expectedResult.getState()));
        assertThat("Result values aren't correct.", result.getResults(), is(expectedResult.getResults()));
        assertThat("Retrieved data amount isn't correct.", result.getRetrievedDataAmount(), is(expectedResult.getRetrievedDataAmount()));
        assertThat("Result signifier isn't correct.", result.getResultSignifier(), is(expectedResult.getResultSignifier()));
        assertThat("Unit isn't correct.", result.getUnit(), is(expectedResult.getUnit()));
        assertThat("Value decimals aren't correct.", result.getValueDecimals(), is(expectedResult.getValueDecimals()));
    }

    @Test(timeout=2000)
    public void testQueryTimeouting() {
        receivedElementOrFinished = false;
        receivedAbort = false;
        
        Collection<Number> dataSource = new ArrayList<>();
        dataSource.add(new Number(10));
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                dataSource, stringMessages, Locale.ENGLISH, AdditionalQueryData.NULL_INSTANCE) {
            @SuppressWarnings("unchecked")
            @Override
            protected Processor<Iterable<Number>, ?> createFirstProcessor() {
                Collection<Processor<Double, ?>> resultReceivers = new ArrayList<>();
                resultReceivers.add(new AbortResultReceiver(this.getResultReceiver()));
                return new BlockingProcessor<Iterable<Number>, Double>((Class<Iterable<Number>>)(Class<?>) Iterable.class, Double.class,
                        ConcurrencyTestsUtil.getExecutor(), resultReceivers, 1000) {
                            @Override
                            protected Double createResult(Iterable<Number> element) {
                                return 0.0;
                            }
                };
            }
        };
        
        try {
            query.run(500, TimeUnit.MILLISECONDS);
            fail("The previous line should throw a timeout exception");
        } catch (TimeoutException e) {
            // A timeout exception is expected
            LOGGER.log(Level.INFO, "The query timed out: ", e);
        }
        assertThat(query.getState(), is(QueryState.TIMED_OUT));
        
        ConcurrencyTestsUtil.sleepFor(1000); // Wait if a result is received
        assertThat("The processing should be aborted, but received elements", receivedElementOrFinished, is(false));
        assertThat("The processing should be aborted, but didn't receive abort", receivedAbort, is(true));
    }

    @Test(timeout=2000)
    public void testQueryAbortion() throws InterruptedException {
        receivedElementOrFinished = false;
        receivedAbort = false;

        Collection<Number> dataSource = new ArrayList<>();
        dataSource.add(new Number(10));
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                dataSource, stringMessages, Locale.ENGLISH, AdditionalQueryData.NULL_INSTANCE) {
            @SuppressWarnings("unchecked")
            @Override
            protected Processor<Iterable<Number>, ?> createFirstProcessor() {
                Collection<Processor<Double, ?>> resultReceivers = new ArrayList<>();
                resultReceivers.add(new AbortResultReceiver(this.getResultReceiver()));
                return new BlockingProcessor<Iterable<Number>, Double>((Class<Iterable<Number>>)(Class<?>) Iterable.class, Double.class,
                        ConcurrencyTestsUtil.getExecutor(), resultReceivers, 1000) {
                            @Override
                            protected Double createResult(Iterable<Number> element) {
                                return 0.0;
                            }
                };
            }
        };
        
        Thread queryRunner = new Thread(new Runnable() {
            @Override
            public void run() {
                resultAfterAbortion = query.run();
            }
        });
        queryRunner.start();
        ConcurrencyTestsUtil.sleepFor(250);
        assertThat(query.getState(), is(QueryState.RUNNING));
        ConcurrencyTestsUtil.sleepFor(250);
        query.abort();
        
        ConcurrencyTestsUtil.sleepFor(1000); // Wait if a result is received
        queryRunner.join();
        assertThat(resultAfterAbortion.getState(), is(QueryResultState.ABORTED));
        assertThat("The processing should be aborted, but received elements", receivedElementOrFinished, is(false));
        assertThat("The processing should be aborted, but didn't receive abort", receivedAbort, is(true));
    }
    
    private class AbortResultReceiver extends NullProcessor<Double, Void> {

        private final Processor<Map<GroupKey, Double>, Void> queryResultReceiver;
        
        public AbortResultReceiver(Processor<Map<GroupKey, Double>, Void> queryResultReceiver) {
            super(Double.class, Void.class);
            this.queryResultReceiver = queryResultReceiver;
        }
        
        @Override
        public void processElement(Double element) {
            receivedElementOrFinished = true;
            queryResultReceiver.processElement(null);
        }
        @Override
        public void finish() throws InterruptedException {
            receivedElementOrFinished = true;
            queryResultReceiver.finish();
        }
        @Override
        public void abort() {
            receivedAbort = true;
            queryResultReceiver.abort();
        }
        
    }

    @Test
    public void testQueryWithTimeoutAndNonBlockingProcess() throws TimeoutException {
        final String keyValue = "Sum";
        ProcessorQuery<Double, Iterable<Number>> query = new ProcessorQuery<Double, Iterable<Number>>(
                createDataSource(), stringMessages, Locale.ENGLISH, AdditionalQueryData.NULL_INSTANCE) {
            @SuppressWarnings("unchecked")
            @Override
            protected Processor<Iterable<Number>, ?> createFirstProcessor() {
                Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers = new ArrayList<>();
                resultReceivers.add(this.getResultReceiver());
                return new AbstractParallelProcessor<Iterable<Number>, Map<GroupKey, Double>>((Class<Iterable<Number>>)(Class<?>) Iterable.class,
                                                                                                    (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
                                                                                                    ConcurrencyTestsUtil.getExecutor(),
                                                                                                    resultReceivers) {
                    @Override
                    protected ProcessorInstruction<Map<GroupKey, Double>> createInstruction(final Iterable<Number> element) {
                        return new AbstractProcessorInstruction<Map<GroupKey,Double>>(this) {
                            @Override
                            public Map<GroupKey, Double> computeResult() {
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
        };
        
        Map<GroupKey, Double> expectedResult = new HashMap<>();
        expectedResult.put(new GenericGroupKey<String>(keyValue), 10358.0);
        assertThat(query.run(500, TimeUnit.MILLISECONDS).getResults(), is(expectedResult));
    }
    
    @Test
    public void testQueryWithError() {
        Query<Double> query = new ProcessorQuery<Double, Double>(0.0) {
            @SuppressWarnings("unchecked")
            @Override
            protected Processor<Double, ?> createFirstProcessor() {
                Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers = new ArrayList<>();
                resultReceivers.add(this.getResultReceiver());
                return new AbstractParallelProcessor<Double, Map<GroupKey, Double>>(Double.class,
                                                                                          (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
                                                                                          ConcurrencyTestsUtil.getExecutor(),
                                                                                          resultReceivers) {
                    @Override
                    protected ProcessorInstruction<Map<GroupKey, Double>> createInstruction(Double element) {
                        return new AbstractProcessorInstruction<Map<GroupKey,Double>>(this) {
                            @Override
                            protected Map<GroupKey, Double> computeResult() throws Exception {
                                throw new RejectedExecutionException("This should cause an error during the query processing");
                            }
                        };
                    }
                    @Override
                    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
                    }
                };
            }
        };
        try {
            query.run();
            fail("The previous line should throw a runtime exception");
        } catch (RuntimeException e) {
            // A RuntimeException with a RejectedExecitonException as cause is expected
            if (!(e.getCause() instanceof RejectedExecutionException)) {
                throw e;
            }
            LOGGER.log(Level.INFO, "The query had an error: ", e);
        }
        assertThat(query.getState(), is(QueryState.ERROR));
    }
    
    @Test
    public void testQueryWithFailure() {
        Query<Double> query = new ProcessorQuery<Double, Double>(0.0) {
            @SuppressWarnings("unchecked")
            @Override
            protected Processor<Double, ?> createFirstProcessor() {
                Collection<Processor<Map<GroupKey, Double>, ?>> resultReceivers = new ArrayList<>();
                resultReceivers.add(this.getResultReceiver());
                return new AbstractParallelProcessor<Double, Map<GroupKey, Double>>(Double.class,
                                                                                          (Class<Map<GroupKey, Double>>)(Class<?>) Map.class,
                                                                                          ConcurrencyTestsUtil.getExecutor(),
                                                                                          resultReceivers) {
                    @Override
                    protected ProcessorInstruction<Map<GroupKey, Double>> createInstruction(Double element) {
                        return new AbstractProcessorInstruction<Map<GroupKey,Double>>(this) {
                            @Override
                            protected Map<GroupKey, Double> computeResult() throws Exception {
                                throw new NullPointerException("This should cause a failure during the query processing");
                            }
                        };
                    }
                    @Override
                    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
                    }
                };
            }
        };
        query.run();
        assertThat(query.getState(), is(QueryState.FAILURE));
    }
    
    @Test
    public void testQueryAdditionalData() {
        AdditionalQueryData additionalData = new AdditionalStatisticQueryData(UUID.randomUUID());
        Query<?> query = new ProcessorQuery<Double, Double>(0.0, additionalData) {
            @Override
            protected Processor<Double, ?> createFirstProcessor() {
                return null;
            }
        };
        assertThat(query.getAdditionalData(), is(additionalData));
        assertThat(query.getAdditionalData(AdditionalStatisticQueryData.class), is(additionalData));
        assertThat(query.getAdditionalData(AdditionalDimensionValuesQueryData.class), nullValue());
        
        additionalData = new AdditionalDimensionValuesQueryData(UUID.randomUUID(), new ArrayList<Function<?>>());
        query = new ProcessorQuery<Double, Double>(0.0, additionalData) {
            @Override
            protected Processor<Double, ?> createFirstProcessor() {
                return null;
            }
        };
        assertThat(query.getAdditionalData(AdditionalDimensionValuesQueryData.class), is(additionalData));
    }

}
