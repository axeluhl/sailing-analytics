package com.sap.sse.datamining.factories;

import java.lang.reflect.Method;

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

}
