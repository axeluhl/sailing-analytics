package com.sap.sse.datamining.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.test.domain.Test_Boat;
import com.sap.sse.datamining.test.domain.Test_BoatClass;
import com.sap.sse.datamining.test.domain.Test_Competitor;
import com.sap.sse.datamining.test.domain.Test_Leg;
import com.sap.sse.datamining.test.domain.Test_Race;
import com.sap.sse.datamining.test.domain.Test_Regatta;
import com.sap.sse.datamining.test.domain.Test_Team;
import com.sap.sse.datamining.test.domain.impl.Test_BoatClassImpl;
import com.sap.sse.datamining.test.domain.impl.Test_BoatImpl;
import com.sap.sse.datamining.test.domain.impl.Test_CompetitorImpl;
import com.sap.sse.datamining.test.domain.impl.Test_LegImpl;
import com.sap.sse.datamining.test.domain.impl.Test_RaceImpl;
import com.sap.sse.datamining.test.domain.impl.Test_RegattaImpl;
import com.sap.sse.datamining.test.domain.impl.Test_TeamImpl;

public final class ComponentTestsUtil {
    
    private final static ProcessorFactory processorFactory = new ProcessorFactory(ConcurrencyTestsUtil.getSharedExecutor());
    
    public static ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    /**
     * Creates a collection of {@link Test_Regatta Test_Regattas} containing the two regattas
     * <ul>
     *  <li>KW 2014 49er
     *    <ul>
     *     <li>with the {@link Test_BoatClass} 49er</li>
     *     <li>and 3 {@link Test_Race Test_Races} (Race 1-3)
     *      <ul>
     *       <li>with 5 {@link Test_Leg Test_Legs} (with a value 0.0 as distance traveled)</li>
     *       <li>and the competitors
     *        <ul>
     *         <li>GER1 (49er Team 1)</li>
     *         <li>GER2 (49er Team 2)</li>
     *         <li>GER3 (49er Team 3)</li>
     *         <li>GER4 (49er Team 4)</li>
     *        </ul>
     *       </li>
     *      </ul>
     *     </li>
     *    </ul>
     *  </li>
     *  <li>KW 2014 505 (with the {@link Test_BoatClass} 505)
     *    <ul>
     *     <li>with the {@link Test_BoatClass} 505</li>
     *     <li>and 2 {@link Test_Race Test_Races} (Race 1-2)
     *      <ul>
     *       <li>with 5 {@link Test_Leg Test_Legs} (with a value 0.0 as distance traveled)</li>
     *       <li>and the competitors
     *        <ul>
     *         <li>ENG1 (505 Team 1)</li>
     *         <li>ENG2 (505 Team 2)</li>
     *         <li>ENG3 (505 Team 3)</li>
     *        </ul>
     *       </li>
     *      </ul>
     *     </li>
     *    </ul></li>
     * </ul>
     * @return an example data source for testing
     */
    public static Collection<Test_Regatta> createExampleDataSource() {
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
        
        Collection<Test_Regatta> dataSource = new ArrayList<>();
        dataSource.add(regatta49er);
        dataSource.add(regatta505);
        return dataSource;
    }
    
}
