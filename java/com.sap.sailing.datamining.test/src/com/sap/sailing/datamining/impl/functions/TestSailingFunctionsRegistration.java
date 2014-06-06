package com.sap.sailing.datamining.impl.functions;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sailing.datamining.impl.data.SailingDataMiningClassesWithFunctionsService;
import com.sap.sailing.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public class TestSailingFunctionsRegistration {
    
    private static FunctionRegistry functionRegistry;
    private static FunctionProvider functionProvider;
    
    @BeforeClass
    public static void setUpFunctionRegistryAndProvider() {
        SailingDataMiningClassesWithFunctionsService classesWithFunctionsService = new SailingDataMiningClassesWithFunctionsService();
        
        functionRegistry = FunctionFactory.createFunctionRegistry(ConcurrencyTestsUtil.getExecutor());
        functionRegistry.registerAllWithInternalFunctionPolicy(classesWithFunctionsService.getInternalClassesWithMarkedMethods());
        functionProvider = FunctionFactory.createRegistryFunctionProvider(functionRegistry);
    }

    @Test
    public void testRegisteredDimensionsWithoutTheTransitiveOnes() {
        Collection<Function<?>> expectedDimensions = createExpectedRegisteredDimensionsWithoutTheTransitiveOnes();
        assertThat(functionRegistry.getDimensions(), is(expectedDimensions));
    }

    private Collection<Function<?>> createExpectedRegisteredDimensionsWithoutTheTransitiveOnes() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Test
    public void testImportantRegisteredFunctions() {
        // This tests for the methods marked as SideEffectFreeValue, that will be used for the data extraction
        Function<?>[] expectedFunctions = createExpectedImportantRegisteredFunctions();
        assertThat(functionRegistry.getStatistics(), hasItems(expectedFunctions));
    }

    private Function<?>[] createExpectedImportantRegisteredFunctions() {
        // TODO Auto-generated method stub
        return null;
    }

}
