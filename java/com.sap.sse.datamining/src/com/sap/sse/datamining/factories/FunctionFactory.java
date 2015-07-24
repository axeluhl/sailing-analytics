package com.sap.sse.datamining.factories;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.ConcatenatingCompoundFunction;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;

public class FunctionFactory {
    
    /**
     * Creates a {@link MethodWrappingFunction} for the given method.<br>
     * Throws a {@link ClassCastException}, if the return type of the method doesn't match the generic <code>ReturnType</code> parameter.
     * 
     * @throws ClassCastException
     */
    @SuppressWarnings("unchecked")
    public <ReturnType> MethodWrappingFunction<ReturnType> createMethodWrappingFunction(Method method) throws ClassCastException {
        return new MethodWrappingFunction<ReturnType>(method, (Class<ReturnType>) method.getReturnType());
    }

    /**
     * Creates a {@link ConcatenatingCompoundFunction} for the given functions.<br>
     * Throws a {@link ClassCastException}, if the return type of the last function doesn't match the generic <code>ReturnType</code> parameter.
     * 
     * @throws ClassCastException
     */
    @SuppressWarnings("unchecked")
    public <ReturnType> Function<ReturnType> createCompoundFunction(List<Function<?>> functions) throws ClassCastException {
        return new ConcatenatingCompoundFunction<ReturnType>(functions, (Class<ReturnType>) functions.get(functions.size() - 1).getReturnType());
    }

    public Function<?> createCompoundFunction(List<Function<?>> previousFunctions, Function<?> lastFunction) {
        List<Function<?>> functions = new ArrayList<>(previousFunctions);
        functions.add(lastFunction);
        return createCompoundFunction(functions);
    }

}
