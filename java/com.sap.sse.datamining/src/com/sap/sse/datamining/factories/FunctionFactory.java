package com.sap.sse.datamining.factories;

import java.lang.reflect.Method;
import java.util.List;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.CompoundFunction;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;

public class FunctionFactory {
    
    private FunctionFactory() { }

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

}
