package com.sap.sailing.datamining.factories;

import java.lang.reflect.Method;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;

public class FunctionFactory {
    
    private FunctionFactory() { }

    public static Function createMethodWrappingFunction(Method method) {
        return new MethodWrappingFunction(method);
    }

}
