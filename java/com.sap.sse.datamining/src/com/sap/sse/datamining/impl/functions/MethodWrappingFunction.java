package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public class MethodWrappingFunction<ReturnType> extends AbstractFunction<ReturnType> {

    private final Method method;
    private Class<ReturnType> returnType;
    private AdditionalFunctionData additionalData;
    
    /**
     * Throws an {@link IllegalArgumentException}, if the return type of the method and the given <code>returnType</code>
     * aren't equal.
     */
    public MethodWrappingFunction(Method method, Class<ReturnType> returnType) throws IllegalArgumentException {
        super(isMethodADimension(method));
        checkThatReturnTypesMatch(method, returnType);
        
        this.method = method;
        this.returnType = returnType;
        initializeAdditionalData();
    }

    private static boolean isMethodADimension(Method method) {
        return method.getAnnotation(Dimension.class) != null;
    }

    private void checkThatReturnTypesMatch(Method method, Class<ReturnType> returnType) {
        if (!method.getReturnType().equals(returnType)) {
            throw new IllegalArgumentException("The method return type " + method.getReturnType().getName()
                                             + " and expected return type " + returnType.getName() + " don't match");
        }
    }

    private void initializeAdditionalData() {
        if (method.getAnnotation(Dimension.class) != null) {
            Dimension dimensionData = method.getAnnotation(Dimension.class);
            additionalData = new AdditionalFunctionData(dimensionData.messageKey(), dimensionData.resultUnit(), dimensionData.resultDecimals());
        }
        if (method.getAnnotation(SideEffectFreeValue.class) != null) {
            SideEffectFreeValue valueData = method.getAnnotation(SideEffectFreeValue.class);
            additionalData = new AdditionalFunctionData(valueData.messageKey(), valueData.resultUnit(), valueData.resultDecimals());
        }
    }

    @Override
    public Class<?> getDeclaringType() {
        return method.getDeclaringClass();
    }
    
    @Override
    public Iterable<Class<?>> getParameters() {
        return Arrays.asList(method.getParameterTypes());
    }
    
    @Override
    public Class<ReturnType> getReturnType() {
        return returnType;
    }
    
    @Override
    public ReturnType tryToInvoke(Object instance) {
        return tryToInvoke(instance, new Object[0]);
    }
    
    @SuppressWarnings("unchecked") // The cast has to work, because the constructor checks, that the return types match
    @Override
    public ReturnType tryToInvoke(Object instance, Object... parameters) {
        try {
            return (ReturnType) method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            getLogger().log(Level.FINER, "Error invoking the Function " + getMethodName(), e);
        }
        return null;
    }

    @Override
    public Unit getResultUnit() {
        return additionalData.getResultUnit();
    }

    @Override
    public int getResultDecimals() {
        return additionalData.getResultDecimals();
    }

    @Override
    public String getSimpleName() {
        return method.getName();
    }
    
    @Override
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        if (additionalData == null || additionalData.getMessageKey().isEmpty()) {
            return getSimpleName();
        }
        return stringMessages.get(locale, additionalData.getMessageKey());
    }
    
    private String getMethodName() {
        return method.getName();
    }

    @Override
    public String toString() {
        return getDeclaringType().getSimpleName() + "." + getMethodName() + "(" + parametersAsString() + ") : " + method.getReturnType().getSimpleName();
    }

    private String parametersAsString() {
        StringBuilder parameterBuilder = new StringBuilder();
        boolean first = true;
        for (Class<?> parameterType : getParameters()) {
            if (!first) {
                parameterBuilder.append(", ");
            }
            parameterBuilder.append(parameterType.getSimpleName());
            first = false;
        }
        return parameterBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodWrappingFunction<?> other = (MethodWrappingFunction<?>) obj;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }

}
