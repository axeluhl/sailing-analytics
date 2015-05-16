package com.sap.sse.datamining.impl.components.management;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.AdditionalDimensionValuesQueryData;
import com.sap.sse.datamining.impl.AdditionalOtherQueryData;
import com.sap.sse.datamining.impl.AdditionalStatisticQueryData;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestServerQueryManagement {
    
    private static final FunctionFactory functionFactory = new FunctionFactory();
    private DataMiningServer server;
    
    @Before
    public void initializeServer() {
        server = TestsUtil.createNewServer();
    }
    
    @Test
    public void testStatisticQueryManagementWithConflict() {
        DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());
        AdditionalQueryData additionalData = new AdditionalStatisticQueryData(new UUID(0, 0));
        
        final ControllablePseudoQuery<Double> firstQuery = new ControllablePseudoQuery<Double>(additionalData);
        final ControllablePseudoQuery<Double> secondQuery = new ControllablePseudoQuery<Double>(additionalData);
        runQueriesAndVerifyConflict(session, firstQuery, session, secondQuery);
    }
    
    @Test
    public void testStatisticQueryManagementWithoutConflict() {
        DataMiningSession firstQuerySession = new UUIDDataMiningSession(UUID.randomUUID());
        DataMiningSession secondQuerySession = new UUIDDataMiningSession(UUID.randomUUID());
        AdditionalQueryData additionalData = new AdditionalStatisticQueryData(new UUID(0, 0));
        
        final ControllablePseudoQuery<Double> firstQuery = new ControllablePseudoQuery<Double>(additionalData);
        final ControllablePseudoQuery<Double> secondQuery = new ControllablePseudoQuery<Double>(additionalData);
        runQueriesAndVerifyNoConflict(firstQuerySession, firstQuery, secondQuerySession, secondQuery);
    }
    
    @Test
    public void testDimensionValuesQueryManagementWithConflict() {
        DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());
        Collection<Function<?>> dimensions = new ArrayList<>();
        Method getYear = FunctionTestsUtil.getMethodFromClass(Test_HasRaceContext.class, "getYear");
        dimensions.add(functionFactory.createMethodWrappingFunction(getYear));
        AdditionalQueryData additionalData = new AdditionalDimensionValuesQueryData(new UUID(0, 0), dimensions);
        
        final ControllablePseudoQuery<Double> firstQuery = new ControllablePseudoQuery<Double>(additionalData);
        final ControllablePseudoQuery<Double> secondQuery = new ControllablePseudoQuery<Double>(additionalData);
        runQueriesAndVerifyConflict(session, firstQuery, session, secondQuery);
    }

    @Test
    public void testDimensionValuesQueryManagementWithoutConflictsDueToDifferentDimensions() {
        DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());

        Collection<Function<?>> firstQueryDimensions = new ArrayList<>();
        Method getYear = FunctionTestsUtil.getMethodFromClass(Test_HasRaceContext.class, "getYear");
        firstQueryDimensions.add(functionFactory.createMethodWrappingFunction(getYear));
        AdditionalQueryData firstQueryAdditionalData = new AdditionalDimensionValuesQueryData(new UUID(0, 0), firstQueryDimensions);
        final ControllablePseudoQuery<Double> firstQuery = new ControllablePseudoQuery<Double>(firstQueryAdditionalData);

        Collection<Function<?>> secondQueryDimensions = new ArrayList<>();
        Method getLegNumber = FunctionTestsUtil.getMethodFromClass(Test_HasLegOfCompetitorContext.class, "getLegNumber");
        secondQueryDimensions.add(functionFactory.createMethodWrappingFunction(getLegNumber));
        AdditionalQueryData secondQueryAdditionalData = new AdditionalDimensionValuesQueryData(new UUID(0, 0), secondQueryDimensions);
        final ControllablePseudoQuery<Double> secondQuery = new ControllablePseudoQuery<Double>(secondQueryAdditionalData);
        
        runQueriesAndVerifyNoConflict(session, firstQuery, session, secondQuery);
    }

    @Test
    public void testDimensionValuesQueryManagementWithoutConflictsDueToDifferentSessions() {
        DataMiningSession firstQuerySession = new UUIDDataMiningSession(UUID.randomUUID());
        DataMiningSession secondQuerySession = new UUIDDataMiningSession(UUID.randomUUID());
        Collection<Function<?>> dimensions = new ArrayList<>();
        Method getYear = FunctionTestsUtil.getMethodFromClass(Test_HasRaceContext.class, "getYear");
        dimensions.add(functionFactory.createMethodWrappingFunction(getYear));
        
        AdditionalQueryData firstQueryAdditionalData = new AdditionalDimensionValuesQueryData(new UUID(0, 0), dimensions);
        final ControllablePseudoQuery<Double> firstQuery = new ControllablePseudoQuery<Double>(firstQueryAdditionalData);
        AdditionalQueryData secondQueryAdditionalData = new AdditionalDimensionValuesQueryData(new UUID(0, 0), dimensions);
        final ControllablePseudoQuery<Double> secondQuery = new ControllablePseudoQuery<Double>(secondQueryAdditionalData);
        
        runQueriesAndVerifyNoConflict(firstQuerySession, firstQuery, secondQuerySession, secondQuery);
    }
    
    @Test 
    public void testOtherQueryManagement() {
        DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());
        AdditionalQueryData additionalData = new AdditionalOtherQueryData(new UUID(0, 0));
        
        final ControllablePseudoQuery<Double> firstQuery = new ControllablePseudoQuery<Double>(additionalData);
        final ControllablePseudoQuery<Double> secondQuery = new ControllablePseudoQuery<Double>(additionalData);
        runQueriesAndVerifyNoConflict(session, firstQuery, session, secondQuery);
    }
    
    private void runQueriesAndVerifyConflict(DataMiningSession firstQuerySession, ControllablePseudoQuery<?> firstQuery, DataMiningSession secondQuerySession, ControllablePseudoQuery<?> secondQuery) {
        Thread firstQueryWorker = new Thread(new QueryWorker(firstQuerySession, firstQuery));
        Thread secondQueryWorker = new Thread(new QueryWorker(secondQuerySession, secondQuery));
        
        firstQueryWorker.start();
        ConcurrencyTestsUtil.sleepFor(75);
        assertThat(firstQuery.getState(), not(QueryState.ABORTED));
        secondQueryWorker.start();
        ConcurrencyTestsUtil.sleepFor(75);
        assertThat(firstQuery.getState(), is(QueryState.ABORTED));

        secondQuery.enableProcess();
        ConcurrencyTestsUtil.sleepFor(110);
        assertThat(secondQuery.getState(), is(QueryState.NORMAL));
    }
    
    private void runQueriesAndVerifyNoConflict(DataMiningSession firstQuerySession, ControllablePseudoQuery<?> firstQuery, DataMiningSession secondQuerySession, ControllablePseudoQuery<?> secondQuery) {
        Thread firstQueryWorker = new Thread(new QueryWorker(firstQuerySession, firstQuery));
        Thread secondQueryWorker = new Thread(new QueryWorker(secondQuerySession, secondQuery));
        
        firstQueryWorker.start();
        ConcurrencyTestsUtil.sleepFor(75);
        secondQueryWorker.start();
        ConcurrencyTestsUtil.sleepFor(75);
        assertThat(firstQuery.getState(), not(QueryState.ABORTED));
        
        firstQuery.enableProcess();
        secondQuery.enableProcess();
        ConcurrencyTestsUtil.sleepFor(110);
        assertThat(firstQuery.getState(), is(QueryState.NORMAL));
        assertThat(secondQuery.getState(), is(QueryState.NORMAL));
    }
    
    @Test
    public void testControllablePseudoQuery() {
        AdditionalQueryData additionalData = new AdditionalOtherQueryData(new UUID(0, 0));
        
        final ControllablePseudoQuery<Double> normalQuery = new ControllablePseudoQuery<Double>(additionalData);
        assertThat(normalQuery.getState(), is(QueryState.NOT_STARTED));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                normalQuery.run();
            }
        });
        thread.start();
        ConcurrencyTestsUtil.sleepFor(10);
        assertThat(normalQuery.getState(), is(QueryState.RUNNING));
        normalQuery.enableProcess();
        ConcurrencyTestsUtil.sleepFor(110);
        assertThat(normalQuery.getState(), is(QueryState.NORMAL));
        normalQuery.abort();
        ConcurrencyTestsUtil.sleepFor(50);
        assertThat(normalQuery.getState(), is(QueryState.NORMAL));
        
        final Query<Double> abortedQuery = new ControllablePseudoQuery<Double>(additionalData);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                abortedQuery.run();
            }
        });
        thread.start();
        abortedQuery.abort();
        ConcurrencyTestsUtil.sleepFor(110);
        assertThat(abortedQuery.getState(), is(QueryState.ABORTED));
    }
    
    private static class ControllablePseudoQuery<AggregatedType> implements Query<AggregatedType> {

        private final AdditionalQueryData additionalData;
        private QueryState state;
        
        private boolean processEnabled;
        private boolean aborted;

        public ControllablePseudoQuery(AdditionalQueryData additionalData) {
            this.additionalData = additionalData;
            state = QueryState.NOT_STARTED;
        }

        @Override
        public QueryState getState() {
            return state;
        }

        @Override
        public AdditionalQueryData getAdditionalData() {
            return additionalData;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends AdditionalQueryData> T getAdditionalData(Class<T> additionalDataType) {
            if (additionalDataType.isAssignableFrom(getAdditionalData().getClass())) {
                return (T) getAdditionalData();
            }
            return null;
        }
        
        public void enableProcess() {
            processEnabled = true;
        }

        @Override
        public QueryResult<AggregatedType> run() {
            state = QueryState.RUNNING;
            while (!processEnabled && !aborted) {
                ConcurrencyTestsUtil.sleepFor(100);
            }
            if (processEnabled) {
                state = QueryState.NORMAL;
            } else if (aborted) {
                state = QueryState.ABORTED;
            } else {
                throw new IllegalStateException("The query shouldn't finish, without being aborted "
                        + "or enabled process");
            }
            return null;
        }

        @Override
        public QueryResult<AggregatedType> run(long timeout, TimeUnit unit) throws TimeoutException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void abort() {
            aborted = true;
        }
        
    }
    
    private class QueryWorker implements Runnable {
        
        private final DataMiningSession session;
        private final Query<?> query;

        public QueryWorker(DataMiningSession session, Query<?> query) {
            this.session = session;
            this.query = query;
        }

        @Override
        public void run() {
            server.runNewQueryAndAbortPreviousQueries(session, query);
        }
        
    }

    @Test
    public void testNullSessionOrQuery() {
        try {
            server.runNewQueryAndAbortPreviousQueries(null, null);
            fail("Expected a NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());
            server.runNewQueryAndAbortPreviousQueries(session , null);
            fail("Expected a NullPointerException");
        } catch (NullPointerException e) {
        }
        
        try {
            Query<Double> query = new NullQuery<Double>();
            server.runNewQueryAndAbortPreviousQueries(null, query);
            fail("Expected a NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    
    private static class NullQuery<AggregatedType> implements Query<AggregatedType> {
        @Override
        public QueryState getState() {
            return QueryState.NOT_STARTED;
        }
        @Override
        public AdditionalQueryData getAdditionalData() {
            return new AdditionalStatisticQueryData(new UUID(0, 0));
        }
        @Override
        public <T extends AdditionalQueryData> T getAdditionalData(Class<T> additionalDataType) {
            return null;
        }
        @Override
        public QueryResult<AggregatedType> run() {
            return null;
        }
        @Override
        public QueryResult<AggregatedType> run(long timeout, TimeUnit unit) throws TimeoutException {
            return null;
        }
        @Override
        public void abort() { }
    }

}
