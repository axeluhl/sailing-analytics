package com.sap.sailing.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.SailingDataMiningClassesWithFunctionsService;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Moving;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.common.Named;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;

public class TestSailingFunctionsRegistration {
    
    private static FunctionRegistry functionRegistry;
    
    @BeforeClass
    public static void setUpFunctionRegistryAndProvider() {
        SailingDataMiningClassesWithFunctionsService classesWithFunctionsService = new SailingDataMiningClassesWithFunctionsService();
        
        functionRegistry = new SimpleFunctionRegistry();
        functionRegistry.registerAllWithInternalFunctionPolicy(classesWithFunctionsService.getInternalClassesWithMarkedMethods());
    }
    
    @Test
    public void testImportantRegisteredDimensions() throws NoSuchMethodException, SecurityException {
        Collection<Function<?>> expectedDimensions = createExpectedImportantRegisteredDimensions();
        Collection<Function<?>> providedDimensions = functionRegistry.getDimensions();
        for (Function<?> dimension : expectedDimensions) {
            assertThat("The expected dimension '" + dimension + "' wasn't provided.",
                       providedDimensions.contains(dimension), is(true));
        }
    }

    private Collection<Function<?>> createExpectedImportantRegisteredDimensions() throws NoSuchMethodException, SecurityException {
        Collection<Function<?>> expectedDimensions = new HashSet<>();
        
        Method getRegattaMethod = HasTrackedRaceContext.class.getMethod("getRegatta", new Class<?>[0]);
        Function<?> getRegatta = FunctionFactory.createMethodWrappingFunction(getRegattaMethod);
        Method getNameMethod = Named.class.getMethod("getName", new Class<?>[0]);
        Function<?> getName = FunctionFactory.createMethodWrappingFunction(getNameMethod);
        expectedDimensions.add(FunctionFactory.createCompoundFunction(Arrays.asList(getRegatta, getName)));
        
        Method getYearMethod = HasTrackedRaceContext.class.getMethod("getYear", new Class<?>[0]);
        expectedDimensions.add(FunctionFactory.createMethodWrappingFunction(getYearMethod));
        
        Method getCompetitorMethod = HasTrackedLegOfCompetitorContext.class.getMethod("getCompetitor", new Class<?>[0]);
        Function<?> getCompetitor = FunctionFactory.createMethodWrappingFunction(getCompetitorMethod);
        Method getTeamMethod = Competitor.class.getMethod("getTeam", new Class<?>[0]);
        Function<?> getTeam = FunctionFactory.createMethodWrappingFunction(getTeamMethod);
        Method getNationalityMethod = Team.class.getMethod("getNationality", new Class<?>[0]);
        Function<?> getNationality = FunctionFactory.createMethodWrappingFunction(getNationalityMethod);
        Method getAcronymMethod = Nationality.class.getMethod("getThreeLetterIOCAcronym", new Class<?>[0]);
        Function<?> getAcronym = FunctionFactory.createMethodWrappingFunction(getAcronymMethod);
        expectedDimensions.add(FunctionFactory.createCompoundFunction(Arrays.asList(getCompetitor, getTeam, getNationality, getAcronym)));
        
        return expectedDimensions;
    }
    
    @Test
    public void testImportantRegisteredStatistics() throws NoSuchMethodException, SecurityException {
        Collection<Function<?>> expectedStatistics = createExpectedImportantRegisteredStatistics();
        Collection<Function<?>> providedStatistics = functionRegistry.getStatistics();
        for (Function<?> statistic : expectedStatistics) {
            assertThat("The expected statistic '" + statistic + "' wasn't provided.",
                       providedStatistics.contains(statistic), is(true));
        }
    }

    private Collection<Function<?>> createExpectedImportantRegisteredStatistics() throws NoSuchMethodException, SecurityException {
        Collection<Function<?>> expectedStatistics = new HashSet<>();
        
        Method getDistanceTraveledMethod = HasTrackedLegOfCompetitorContext.class.getMethod("getDistanceTraveled", new Class<?>[0]);
        expectedStatistics.add(FunctionFactory.createMethodWrappingFunction(getDistanceTraveledMethod));
        
        Method getGPSFixMethod = HasGPSFixContext.class.getMethod("getGPSFix", new Class<?>[0]);
        Function<?> getGPSFix = FunctionFactory.createMethodWrappingFunction(getGPSFixMethod);
        Method getSpeedMethod = Moving.class.getMethod("getSpeed", new Class<?>[0]);
        Function<?> getSpeed = FunctionFactory.createMethodWrappingFunction(getSpeedMethod);
        Method getKnotsMethod = Speed.class.getMethod("getKnots", new Class<?>[0]);
        Function<?> getKnots = FunctionFactory.createMethodWrappingFunction(getKnotsMethod);
        expectedStatistics.add(FunctionFactory.createCompoundFunction(Arrays.asList(getGPSFix, getSpeed, getKnots)));
        
        return expectedStatistics;
    }

}
