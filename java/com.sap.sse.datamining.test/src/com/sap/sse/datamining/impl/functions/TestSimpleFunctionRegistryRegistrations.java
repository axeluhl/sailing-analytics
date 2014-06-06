package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Boat;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Named;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;


public class TestSimpleFunctionRegistryRegistrations {
    
    private Set<Class<?>> internalClassesToScan;
    private HashSet<Class<?>> externalClassesToScan;
    
    @Before
    public void initializeClassesToScan() {
        internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        
        externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
    }
    
    @Test
    public void testRegistration() throws NoSuchMethodException, SecurityException {
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        registry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
        
        Collection<Function<?>> expectedDimensions = createExpectedDimensions();
        assertThat(registry.getDimensions(), is(expectedDimensions));
        
        Collection<Function<?>> expectedStatistics = createExpectedStatistics();
        assertThat(registry.getStatistics(), is(expectedStatistics));
        
        Collection<Function<?>> expectedExternalFunctions = createExpectedExternalFunctions();
        assertThat(registry.getExternalFunctions(), is(expectedExternalFunctions));
    }

    private Collection<Function<?>> createExpectedDimensions() throws NoSuchMethodException, SecurityException {
        Collection<Function<?>> dimensions = new HashSet<>();
        
        Method getNameMethod = Test_Named.class.getMethod("getName", new Class<?>[0]);
        Function<?> getName = FunctionFactory.createMethodWrappingFunction(getNameMethod);
        
        Method getRegattaMethod = Test_HasRaceContext.class.getMethod("getRegatta", new Class<?>[0]);
        Function<?> getRegatta = FunctionFactory.createMethodWrappingFunction(getRegattaMethod);
        dimensions.add(FunctionFactory.createCompoundFunction(null, Arrays.asList(getRegatta, getName)));

        Method getRaceMethod = Test_HasRaceContext.class.getMethod("getRace", new Class<?>[0]);
        Function<?> getRace = FunctionFactory.createMethodWrappingFunction(getRaceMethod);
        dimensions.add(FunctionFactory.createCompoundFunction(null, Arrays.asList(getRace, getName)));
        
        Method getBoatClassMethod = Test_HasRaceContext.class.getMethod("getBoatClass", new Class<?>[0]);
        Function<?> getBoatClass = FunctionFactory.createMethodWrappingFunction(getBoatClassMethod);
        dimensions.add(FunctionFactory.createCompoundFunction(null, Arrays.asList(getBoatClass, getName)));
        
        Method getYearMethod = Test_HasRaceContext.class.getMethod("getYear", new Class<?>[0]);
        dimensions.add(FunctionFactory.createMethodWrappingFunction(getYearMethod));
        
        Method getLegNumberMethod = Test_HasLegContext.class.getMethod("getLegNumber", new Class<?>[0]);
        dimensions.add(FunctionFactory.createMethodWrappingFunction(getLegNumberMethod));
        
        Method getCompetitorMethod = Test_HasLegContext.class.getMethod("getCompetitor", new Class<?>[0]);
        Function<?> getCompetitor = FunctionFactory.createMethodWrappingFunction(getCompetitorMethod);
        
        Method getTeamMethod = Test_Competitor.class.getMethod("getTeam", new Class<?>[0]);
        Function<?> getTeam = FunctionFactory.createMethodWrappingFunction(getTeamMethod);
        dimensions.add(FunctionFactory.createCompoundFunction(null, Arrays.asList(getCompetitor, getTeam, getName)));
        
        Method getBoatMethod = Test_Competitor.class.getMethod("getBoat", new Class<?>[0]);
        Function<?> getBoat = FunctionFactory.createMethodWrappingFunction(getBoatMethod);
        Method getSailIDMethod = Test_Boat.class.getMethod("getSailID", new Class<?>[0]);
        Function<?> getSailID = FunctionFactory.createMethodWrappingFunction(getSailIDMethod);
        dimensions.add(FunctionFactory.createCompoundFunction(null, Arrays.asList(getCompetitor, getBoat, getSailID)));
        
        return dimensions;
    }

    private Collection<Function<?>> createExpectedStatistics() throws NoSuchMethodException, SecurityException {
        Method getLegMethod = Test_HasLegContext.class.getMethod("getLeg", new Class<?>[0]);
        Function<?> getLeg = FunctionFactory.createMethodWrappingFunction(getLegMethod);
        Method getDistanceTraveledMethod = Test_Leg.class.getMethod("getDistanceTraveled", new Class<?>[0]);
        Function<?> getDistanceTraveled = FunctionFactory.createMethodWrappingFunction(getDistanceTraveledMethod);
        
        Collection<Function<?>> statistics = new HashSet<>();
        statistics.add(FunctionFactory.createCompoundFunction(null, Arrays.asList(getLeg, getDistanceTraveled)));
        return statistics;
    }

    private Collection<Function<?>> createExpectedExternalFunctions() throws NoSuchMethodException, SecurityException {
        Method fooMethod = Test_ExternalLibraryClass.class.getMethod("foo", new Class<?>[0]);
        Function<?> foo = FunctionFactory.createMethodWrappingFunction(fooMethod);
        
        Collection<Function<?>> externalFunctions = new HashSet<>();
        externalFunctions.add(foo);
        return externalFunctions;
    }

}
