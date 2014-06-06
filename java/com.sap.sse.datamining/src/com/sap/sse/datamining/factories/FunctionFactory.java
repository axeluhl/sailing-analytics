package com.sap.sse.datamining.factories;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.functions.CompoundFunction;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;
import com.sap.sse.datamining.impl.functions.RegistryFunctionProvider;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;

public class FunctionFactory {
    
    private FunctionFactory() { }
    
    public static FunctionRegistry createFunctionRegistry(ExecutorService executor) {
        return new SimpleFunctionRegistry();
    }
    
    public static FunctionProvider createRegistryFunctionProvider(FunctionRegistry... functionRegistries) {
        return new RegistryFunctionProvider(functionRegistries);
    }
    
    public static FunctionProvider createRegistryFunctionProvider(Collection<FunctionRegistry> functionRegistries) {
        return new RegistryFunctionProvider(functionRegistries);
    }

    /**
     * Creates a {@link MethodWrappingFunction} for the given method.<br>
     * Throws a {@link ClassCastException}, if the return type of the method doesn't match the generic <code>ReturnType</code> parameter.
     * 
     * @throws ClassCastException
     */
    @SuppressWarnings("unchecked")
    public static <ReturnType> MethodWrappingFunction<ReturnType> createMethodWrappingFunction(Method method) throws ClassCastException {
        return new MethodWrappingFunction<ReturnType>(method, (Class<ReturnType>) method.getReturnType());
    }
    
    public static <ReturnType> Function<ReturnType> createCompoundFunction(List<Function<?>> functions) {
        return createCompoundFunction(null, functions);
    }

    /**
     * Creates a {@link CompoundFunction} for the given functions.<br>
     * Throws a {@link ClassCastException}, if the return type of the last function doesn't match the generic <code>ReturnType</code> parameter.
     * 
     * @throws ClassCastException
     */
    @SuppressWarnings("unchecked")
    public static <ReturnType> Function<ReturnType> createCompoundFunction(String name, List<Function<?>> functions) throws ClassCastException {
        return new CompoundFunction<ReturnType>(name, functions, (Class<ReturnType>) functions.get(functions.size() - 1).getReturnType());
    }

    public static Function<?> createCompoundFunction(String name, List<Function<?>> previousFunctions,
            Function<?> lastFunction) {
        List<Function<?>> functions = new ArrayList<>(previousFunctions);
        functions.add(lastFunction);
        return createCompoundFunction(name, functions);
    }

}
