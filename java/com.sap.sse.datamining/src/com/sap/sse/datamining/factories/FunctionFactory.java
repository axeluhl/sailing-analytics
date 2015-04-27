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

    /**
     * Creates a new Function for the given function, without the first method in the list.</br> 
     * @return A new CompoundFunction without the first method in the list or <code>null</code>, if the given
     * function is no CompoundFunction or the method list has the size of 1. 
     */
    public Function<?> trimFirstMethod(Function<?> function) {
        if (function instanceof ConcatenatingCompoundFunction<?>) {
            ConcatenatingCompoundFunction<?> compoundFunction = (ConcatenatingCompoundFunction<?>) function;
            List<Function<?>> methodList = compoundFunction.getFunctions();
            if (methodList.size() > 1) {
                List<Function<?>> trimmedMethodList = new ArrayList<>();
                for (int i = 1; i < methodList.size(); i++) {
                    trimmedMethodList.add(methodList.get(i));
                }
                return trimmedMethodList.size() == 1 ? trimmedMethodList.get(0) : createCompoundFunction(trimmedMethodList);
            }
        }
        
        return null;
    }

}
