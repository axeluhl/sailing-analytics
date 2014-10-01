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

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ParallelByDimensionGroupingProcessor;
import com.sap.sse.datamining.impl.components.ParallelGroupedElementsValueExtractionProcessor;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDataCollectingAsSetProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Boat;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_BoatClass;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Named;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Team;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_BoatClassImpl;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_BoatImpl;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_CompetitorImpl;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_LegImpl;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_RaceImpl;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_RegattaImpl;
import com.sap.sse.datamining.test.functions.registry.test_classes.impl.Test_TeamImpl;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContextImpl;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;

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
            protected Processor<Collection<Test_Regatta>> createFirstProcessor() {
                Collection<Processor<Map<GroupKey, Set<Object>>>> collectorResultReceivers = Arrays.asList(/*query*/ this.getResultReceiver());
                
                Processor<GroupedDataEntry<Object>> resultCollector = new ParallelGroupedDataCollectingAsSetProcessor<Object>(ConcurrencyTestsUtil.getExecutor(), collectorResultReceivers);
                Collection<Processor<GroupedDataEntry<Object>>> extractionResultReceivers = new ArrayList<>();
                extractionResultReceivers.add(resultCollector);
                
                Collection<Function<?>> legDimensions = new ArrayList<>();
                legDimensions.add(dimensionLegNumber);
                legDimensions.add(dimensionCompetitorName);
                legDimensions.add(dimensionCompetitorSailID);
                Collection<Processor<Test_HasLegContext>> legRetrieverResultReceivers = createGroupingExtractorsForDimensions(extractionResultReceivers, legDimensions);
                Processor<Test_HasRaceContext> legRetriever = new AbstractSimpleRetrievalProcessor<Test_HasRaceContext, Test_HasLegContext>(ConcurrencyTestsUtil.getExecutor(), legRetrieverResultReceivers) {
                    @Override
                    protected Iterable<Test_HasLegContext> retrieveData(Test_HasRaceContext raceWithContext) {
                        Collection<Test_HasLegContext> legsWithContext = new ArrayList<>();
                        int legNumber = 0;
                        for (Test_Leg leg : raceWithContext.getRace().getLegs()) {
                            legNumber++;
                            for (Test_Competitor competitor : raceWithContext.getRace().getCompetitors()) {
                                legsWithContext.add(new Test_HasLegContextImpl(raceWithContext.getRegatta(), raceWithContext.getRace(), raceWithContext.getBoatClass(),
                                                                               raceWithContext.getYear(), leg, legNumber, competitor));
                            }
                        }
                        return legsWithContext;
                    }
                };

                
                Collection<Function<?>> raceDimensions = new ArrayList<>();
                raceDimensions.add(dimensionRegattaName);
                raceDimensions.add(dimensionRaceName);
                raceDimensions.add(dimensionBoatClassName);
                raceDimensions.add(dimensionYear);
                Collection<Processor<Test_HasRaceContext>> raceRetrieverResultReceivers = createGroupingExtractorsForDimensions(extractionResultReceivers, raceDimensions);
                raceRetrieverResultReceivers.add(legRetriever);
                return new AbstractSimpleRetrievalProcessor<Collection<Test_Regatta>, Test_HasRaceContext>(ConcurrencyTestsUtil.getExecutor(), raceRetrieverResultReceivers) {
                    @Override
                    protected Iterable<Test_HasRaceContext> retrieveData(Collection<Test_Regatta> regattas) {
                        Collection<Test_HasRaceContext> racesWithContext = new ArrayList<>();
                        for (Test_Regatta regatta : regattas) {
                            for (Test_Race race : regatta.getRaces()) {
                                racesWithContext.add(new Test_HasRaceContextImpl(regatta, race, regatta.getBoatClass(), regatta.getYear()));
                            }
                        }
                        return racesWithContext;
                    }
                };
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <DataType> Collection<Processor<DataType>> createGroupingExtractorsForDimensions(
            Collection<Processor<GroupedDataEntry<Object>>> extractionResultReceivers, Collection<Function<?>> dimensions) {
        Collection<Processor<DataType>> groupingExtractors = new ArrayList<>();
        for (Function<?> dimension : dimensions) {
            Processor<GroupedDataEntry<DataType>> dimensionValueExtractor = new ParallelGroupedElementsValueExtractionProcessor<DataType, Object>(ConcurrencyTestsUtil.getExecutor(), extractionResultReceivers, (Function<Object>) dimension);
            Processor<DataType> byDimensionGrouper = new ParallelByDimensionGroupingProcessor<>(ConcurrencyTestsUtil.getExecutor(), Arrays.asList(dimensionValueExtractor), dimension);
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
        
        Method getLegNumberMethod = Test_HasLegContext.class.getMethod("getLegNumber", new Class<?>[0]);
        dimensionLegNumber = FunctionFactory.createMethodWrappingFunction(getLegNumberMethod);
        
        Method getCompetitorMethod = Test_HasLegContext.class.getMethod("getCompetitor", new Class<?>[0]);
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
        //Initialize 49er competitors
        Test_Team team1_49er = new Test_TeamImpl("49er Team 1");
        Test_Boat boatGER1 = new Test_BoatImpl("GER1");
        Test_Competitor competitor1_49er = new Test_CompetitorImpl(team1_49er, boatGER1);
        
        Test_Team team2_49er = new Test_TeamImpl("49er Team 2");
        Test_Boat boatGER2 = new Test_BoatImpl("GER2");
        Test_Competitor competitor2_49er = new Test_CompetitorImpl(team2_49er, boatGER2);
        
        Test_Team team3_49er = new Test_TeamImpl("49er Team 3");
        Test_Boat boatGER3 = new Test_BoatImpl("GER3");
        Test_Competitor competitor3_49er = new Test_CompetitorImpl(team3_49er, boatGER3);
        
        Test_Team team4_49er = new Test_TeamImpl("49er Team 4");
        Test_Boat boatGER4 = new Test_BoatImpl("GER4");
        Test_Competitor competitor4_49er = new Test_CompetitorImpl(team4_49er, boatGER4);
        
        Collection<Test_Competitor> competitors49er = Arrays.asList(competitor1_49er, competitor2_49er, competitor3_49er, competitor4_49er);

        //Initialize 505 competitors
        Test_Team team1_505 = new Test_TeamImpl("505 Team 1");
        Test_Boat boatENG1 = new Test_BoatImpl("ENG1");
        Test_Competitor competitor1_505 = new Test_CompetitorImpl(team1_505, boatENG1);
        
        Test_Team team2_505 = new Test_TeamImpl("505 Team 2");
        Test_Boat boatENG2 = new Test_BoatImpl("ENG2");
        Test_Competitor competitor2_505 = new Test_CompetitorImpl(team2_505, boatENG2);
        
        Test_Team team3_505 = new Test_TeamImpl("505 Team 3");
        Test_Boat boatENG3 = new Test_BoatImpl("ENG3");
        Test_Competitor competitor3_505 = new Test_CompetitorImpl(team3_505, boatENG3);
        
        Collection<Test_Competitor> competitors505 = Arrays.asList(competitor1_505, competitor2_505, competitor3_505);
        
        // Test_Leg has only the statistic distance traveled
        // This test requires only the dimensions, so only one leg is necessary
        Test_Leg leg = new Test_LegImpl(0.0);
        Collection<Test_Leg> legs = Arrays.asList(leg, leg, leg, leg, leg);
        
        //Initialize races, boat classes and regattas
        Test_Race race1_49er = new Test_RaceImpl("Race 1", competitors49er, legs);
        Test_Race race2_49er = new Test_RaceImpl("Race 2", competitors49er, legs);
        Test_Race race3_49er = new Test_RaceImpl("Race 3", competitors49er, legs);
        Test_Race race1_505 = new Test_RaceImpl("Race 1", competitors505, legs);
        Test_Race race2_505 = new Test_RaceImpl("Race 2", competitors505, legs);

        Test_BoatClass boatClass49er = new Test_BoatClassImpl("49er");
        Test_BoatClass boatClass505 = new Test_BoatClassImpl("505");
        
        Test_Regatta regatta49er = new Test_RegattaImpl("KW 2014 49er", boatClass49er, 2014, race1_49er, race2_49er, race3_49er);
        Test_Regatta regatta505 = new Test_RegattaImpl("KW 2014 505", boatClass505, 2014, race1_505, race2_505);
        
        dataSource = new ArrayList<>();
        dataSource.add(regatta49er);
        dataSource.add(regatta505);
    }

}
