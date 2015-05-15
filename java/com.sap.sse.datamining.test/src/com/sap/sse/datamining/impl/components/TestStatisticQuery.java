package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.junit.Test;

import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.impl.AbstractDataSourceProvider;
import com.sap.sse.datamining.impl.ModifiableStatisticQueryDefinition;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.data.Unit;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.QueryResultImpl;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.datamining.test.util.components.Number;
import com.sap.sse.datamining.test.util.components.SingleDataRetrieverChainDefinition;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class TestStatisticQuery {
    
    private static final ResourceBundleStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    @SuppressWarnings("rawtypes")
    @Test
    public void testStatisticQuery() throws InterruptedException, ExecutionException {
        final Collection<Number> dataSource = createDataSource();
        
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        server.addStringMessages(stringMessages);
        server.setDataSourceProvider(new AbstractDataSourceProvider<Collection>(Collection.class) {
            @Override
            public Collection<?> getDataSource() {
                return dataSource;
            }
        });

        Query<Double> query = server.createQuery(createQueryDefinition());
        assertThat(query.getState(), is(QueryState.NOT_STARTED));
        QueryResult<Double> expectedResult = buildExpectedResult(dataSource);
        verifyResult(query.run(), expectedResult);
    }

    /**
     * Creates a query definition, that filters all numbers < 10, groups them by
     * their length, extracts the cross sum and aggregates their sum.
     */
    private StatisticQueryDefinition<Collection<Number>, Number, Double> createQueryDefinition() {
        FunctionFactory functionFactory = FunctionTestsUtil.getFunctionFactory();
        
        @SuppressWarnings("unchecked")
        DataRetrieverChainDefinition<Collection<Number>, Number> retrieverChain = new SingleDataRetrieverChainDefinition<>((Class<Collection<Number>>)(Class<?>) Collection.class, Number.class, "Number");
        retrieverChain.startWith(NumberRetrievalProcessor.class, Number.class, "Number");
        
        Method getCrossSumMethod = FunctionTestsUtil.getMethodFromClass(Number.class, "getCrossSum");
        ModifiableStatisticQueryDefinition<Collection<Number>, Number, Double> definition =
                new ModifiableStatisticQueryDefinition<>(Locale.ENGLISH, retrieverChain, functionFactory.createMethodWrappingFunction(getCrossSumMethod), AggregatorType.Sum);
        
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

}
