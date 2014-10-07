package com.sap.sse.datamining.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelByDimensionGroupingProcessor;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDataCollectingAsSetProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Boat;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Named;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.util.ComponentsAndQueriesTestsUtil;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.TestLegOfCompetitorWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRaceWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRegattaRetrievalProcessor;

public class TestDimensionsValuesQuery {
    
    //Test_HasRaceContext dimensions
    private Function<String> dimensionRegattaName;
    private Function<String> dimensionRaceName;
    private Function<String> dimensionBoatClassName;
    private Function<Integer> dimensionYear;

    //Test_HasLegContext dimensions
    private Function<Integer> dimensionLegNumber;
    private Function<String> dimensionCompetitorName;
    private Function<String> dimensionCompetitorSailID;
    
    private DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition;
    private Collection<Test_Regatta> dataSource;

    @Test
    public void testDimensionsValuesQuery() throws InterruptedException, ExecutionException {
        Query<Set<Object>> dimensionsValueQuery = createDimensionsValuesQuery();
        Map<GroupKey, Set<Object>> expectedResultData = buildExpectedResultData();
        ConcurrencyTestsUtil.verifyResultData(dimensionsValueQuery.run().getResults(), (Map<GroupKey, Set<Object>>) expectedResultData);
    }

    private Query<Set<Object>> createDimensionsValuesQuery() {
        return new ProcessorQuery<Set<Object>, Collection<Test_Regatta>>(ConcurrencyTestsUtil.getExecutor(), dataSource) {
            @Override
            protected Processor<Collection<Test_Regatta>, ?> createFirstProcessor() {
                Collection<Processor<Map<GroupKey, Set<Object>>, ?>> collectorResultReceivers = new ArrayList<>();
                collectorResultReceivers.add(/*query*/ this.getResultReceiver());
                
                Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> resultCollector = new ParallelGroupedDataCollectingAsSetProcessor<Object>(ConcurrencyTestsUtil.getExecutor(), collectorResultReceivers);
                Collection<Processor<GroupedDataEntry<Object>, ?>> extractionResultReceivers = new ArrayList<>();
                extractionResultReceivers.add(resultCollector);
                
                Collection<Function<?>> legDimensions = new ArrayList<>();
                legDimensions.add(dimensionLegNumber);
                legDimensions.add(dimensionCompetitorName);
                legDimensions.add(dimensionCompetitorSailID);
                
                Collection<Function<?>> raceDimensions = new ArrayList<>();
                raceDimensions.add(dimensionRegattaName);
                raceDimensions.add(dimensionRaceName);
                raceDimensions.add(dimensionBoatClassName);
                raceDimensions.add(dimensionYear);
                
                DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
                chainBuilder.stepDeeper();
                for (Processor<?, ?> resultReceiver : createGroupingExtractorsForDimensions(Test_HasRaceContext.class, extractionResultReceivers, raceDimensions)) {
                    chainBuilder.addResultReceiver(resultReceiver);
                }
                
                chainBuilder.stepDeeper();
                for (Processor<?, ?> resultReceiver : createGroupingExtractorsForDimensions(Test_HasLegOfCompetitorContext.class, extractionResultReceivers, legDimensions)) {
                    chainBuilder.addResultReceiver(resultReceiver);
                }
                
                return chainBuilder.build();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <DataType> Collection<Processor<DataType, ?>> createGroupingExtractorsForDimensions(Class<DataType> dataType,
            Collection<Processor<GroupedDataEntry<Object>, ?>> extractionResultReceivers, Collection<Function<?>> dimensions) {
        Collection<Processor<DataType, ?>> groupingExtractors = new ArrayList<>();
        for (Function<?> dimension : dimensions) {
            Processor<GroupedDataEntry<DataType>, GroupedDataEntry<Object>> dimensionValueExtractor = new ParallelGroupedElementsValueExtractionProcessor<DataType, Object>(ConcurrencyTestsUtil.getExecutor(), extractionResultReceivers, (Function<Object>) dimension);
            Collection<Processor<GroupedDataEntry<DataType>, ?>> groupingResultReceivers = new ArrayList<>();
            groupingResultReceivers.add(dimensionValueExtractor);
            
            Processor<DataType, GroupedDataEntry<DataType>> byDimensionGrouper = new ParallelByDimensionGroupingProcessor<>(dataType, ConcurrencyTestsUtil.getExecutor(), groupingResultReceivers, dimension);
            groupingExtractors.add(byDimensionGrouper);
        }
        return groupingExtractors;
    }
    
    private Map<GroupKey, Set<Object>> buildExpectedResultData() {
        Map<GroupKey, Set<Object>> expectedResultData = new HashMap<>();

        //Add empty sets for Test_HasRaceContext dimensions
        GroupKey dimensionRegattaNameGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionRegattaName));
        expectedResultData.put(dimensionRegattaNameGroupKey, new HashSet<Object>());
        GroupKey dimensionRaceNameGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionRaceName));
        expectedResultData.put(dimensionRaceNameGroupKey, new HashSet<Object>());
        GroupKey dimensionBoatClassNameGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionBoatClassName));
        expectedResultData.put(dimensionBoatClassNameGroupKey, new HashSet<Object>());
        GroupKey dimensionYearGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionYear));
        expectedResultData.put(dimensionYearGroupKey, new HashSet<Object>());

        //Add empty sets for Test_HasLegContext dimensions
        GroupKey dimensionLegNumberGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionLegNumber));
        expectedResultData.put(dimensionLegNumberGroupKey, new HashSet<Object>());
        GroupKey dimensionCompetitorNameGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionCompetitorName));
        expectedResultData.put(dimensionCompetitorNameGroupKey, new HashSet<Object>());
        GroupKey dimensionCompetitorSailIDGroupKey = new GenericGroupKey<FunctionDTO>(FunctionDTOFactory.createFunctionDTO(dimensionCompetitorSailID));
        expectedResultData.put(dimensionCompetitorSailIDGroupKey, new HashSet<Object>());
        
        for (Test_Regatta regatta : dataSource) {
            expectedResultData.get(dimensionRegattaNameGroupKey).add(regatta.getName());
            expectedResultData.get(dimensionYearGroupKey).add(regatta.getYear());
            expectedResultData.get(dimensionBoatClassNameGroupKey).add(regatta.getBoatClass().getName());
            
            for (Test_Race race : regatta.getRaces()) {
                expectedResultData.get(dimensionRaceNameGroupKey).add(race.getName());
                
                for (int legNumber = 1; legNumber <= race.getLegs().size(); legNumber++) {
                    expectedResultData.get(dimensionLegNumberGroupKey).add(legNumber);
                }
                
                for (Test_Competitor competitor : race.getCompetitors()) {
                    expectedResultData.get(dimensionCompetitorNameGroupKey).add(competitor.getTeam().getName());
                    expectedResultData.get(dimensionCompetitorSailIDGroupKey).add(competitor.getBoat().getSailID());
                }
            }
        }
        return expectedResultData;
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void initializeDataRetrieverChain() {
        dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class);
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class);
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class);
    }

    @Before
    public void initializeDimensions() throws NoSuchMethodException, SecurityException {
        Method getNameMethod = Test_Named.class.getMethod("getName", new Class<?>[0]);
        Function<?> getName = FunctionFactory.createMethodWrappingFunction(getNameMethod);
        
        Method getRegattaMethod = Test_HasRaceContext.class.getMethod("getRegatta", new Class<?>[0]);
        Function<?> getRegatta = FunctionFactory.createMethodWrappingFunction(getRegattaMethod);
        dimensionRegattaName = FunctionFactory.createCompoundFunction(null, Arrays.asList(getRegatta, getName));

        Method getRaceMethod = Test_HasRaceContext.class.getMethod("getRace", new Class<?>[0]);
        Function<?> getRace = FunctionFactory.createMethodWrappingFunction(getRaceMethod);
        dimensionRaceName = FunctionFactory.createCompoundFunction(null, Arrays.asList(getRace, getName));
        
        Method getBoatClassMethod = Test_HasRaceContext.class.getMethod("getBoatClass", new Class<?>[0]);
        Function<?> getBoatClass = FunctionFactory.createMethodWrappingFunction(getBoatClassMethod);
        dimensionBoatClassName = FunctionFactory.createCompoundFunction(null, Arrays.asList(getBoatClass, getName));
        
        Method getYearMethod = Test_HasRaceContext.class.getMethod("getYear", new Class<?>[0]);
        dimensionYear = FunctionFactory.createMethodWrappingFunction(getYearMethod);
        
        Method getLegNumberMethod = Test_HasLegOfCompetitorContext.class.getMethod("getLegNumber", new Class<?>[0]);
        dimensionLegNumber = FunctionFactory.createMethodWrappingFunction(getLegNumberMethod);
        
        Method getCompetitorMethod = Test_HasLegOfCompetitorContext.class.getMethod("getCompetitor", new Class<?>[0]);
        Function<?> getCompetitor = FunctionFactory.createMethodWrappingFunction(getCompetitorMethod);
        
        Method getTeamMethod = Test_Competitor.class.getMethod("getTeam", new Class<?>[0]);
        Function<?> getTeam = FunctionFactory.createMethodWrappingFunction(getTeamMethod);
        dimensionCompetitorName = FunctionFactory.createCompoundFunction(null, Arrays.asList(getCompetitor, getTeam, getName));
        
        Method getBoatMethod = Test_Competitor.class.getMethod("getBoat", new Class<?>[0]);
        Function<?> getBoat = FunctionFactory.createMethodWrappingFunction(getBoatMethod);
        Method getSailIDMethod = Test_Boat.class.getMethod("getSailID", new Class<?>[0]);
        Function<?> getSailID = FunctionFactory.createMethodWrappingFunction(getSailIDMethod);
        dimensionCompetitorSailID = FunctionFactory.createCompoundFunction(null, Arrays.asList(getCompetitor, getBoat, getSailID));
    }
    
    @Before
    public void initializeDataSource() {
        dataSource = ComponentsAndQueriesTestsUtil.createExampleDataSource();
    }

}
