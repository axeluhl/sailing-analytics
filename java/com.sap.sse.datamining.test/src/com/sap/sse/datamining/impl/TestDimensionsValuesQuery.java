package com.sap.sse.datamining.impl;

import static org.junit.Assert.fail;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Boat;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_BoatClass;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;
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
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;

public class TestDimensionsValuesQuery {
    
    private FunctionRegistry functionRegistry;
    
    @Before
    public void initializeDataPool() {
        //TODO enrich the races with competitors and legs for a realistic test
        Test_Race race1_49er = new Test_RaceImpl("Race 1");
        Test_Race race2_49er = new Test_RaceImpl("Race 2");
        Test_Race race3_49er = new Test_RaceImpl("Race 3");
        Test_Race race1_505 = new Test_RaceImpl("Race 1");
        Test_Race race2_505 = new Test_RaceImpl("Race 2");

        Test_BoatClass boatClass49er = new Test_BoatClassImpl("49er");
        Test_BoatClass boatClass505 = new Test_BoatClassImpl("505");
        
        Test_Regatta regatta49er = new Test_RegattaImpl("KW 2014 49er", boatClass49er, race1_49er, race2_49er, race3_49er);
        Test_Regatta regatta505 = new Test_RegattaImpl("KW 2014 505", boatClass505, race1_505, race2_505);
        
        // Test_Leg has only the statistic distance traveled
        // This test requires only the dimensions, so only one leg is necessary
        Test_Leg leg = new Test_LegImpl(0.0);
        
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
        
        Test_Team team1_505 = new Test_TeamImpl("505 Team 1");
        Test_Boat boatENG1 = new Test_BoatImpl("ENG1");
        Test_Competitor competitor1_505 = new Test_CompetitorImpl(team1_505, boatENG1);
        
        Test_Team team2_505 = new Test_TeamImpl("505 Team 2");
        Test_Boat boatENG2 = new Test_BoatImpl("ENG2");
        Test_Competitor competitor2_505 = new Test_CompetitorImpl(team2_505, boatENG2);
        
        Test_Team team3_505 = new Test_TeamImpl("505 Team 3");
        Test_Boat boatENG3 = new Test_BoatImpl("ENG3");
        Test_Competitor competitor3_505 = new Test_CompetitorImpl(team3_505, boatENG3);
    }

    @Before
    public void initializeFunctionRegistry() {
        HashSet<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        
        functionRegistry = new SimpleFunctionRegistry();
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

}
