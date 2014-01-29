package com.sap.sailing.datamining.function.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

import com.sap.sailing.datamining.DataMiningStringMessages;
import com.sap.sailing.datamining.annotations.Dimension;
import com.sap.sailing.datamining.annotations.SideEffectFreeValue;

public class MethodWrappingFunction<ReturnType> extends AbstractFunction<ReturnType> {

    private Method method;
    
    private String messageKey;
    
    /**
     * Throws an {@link IllegalArgumentException}, if the return type of the method and the given <code>returnType</code>
     * aren't equal.
     */
    public MethodWrappingFunction(Method method, Class<ReturnType> returnType) throws IllegalArgumentException {
        super(isMethodADimension(method));
        checkThatReturnTypesMatch(method, returnType);
        
        this.method = method;
        initializeMessageKey();
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

    private void initializeMessageKey() {
        if (method.getAnnotation(Dimension.class) != null) {
            setMessageKey(method.getAnnotation(Dimension.class).value());
        }
        if (method.getAnnotation(SideEffectFreeValue.class) != null) {
            setMessageKey(method.getAnnotation(SideEffectFreeValue.class).value());
        }
    }

    private void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }
    
    @Override
    public Iterable<Class<?>> getParameters() {
        return Arrays.asList(method.getParameterTypes());
    }
    
    @Override
    public ReturnType invoke(Object instance) {
        return invoke(instance, new Object[0]);
    }
    
    @SuppressWarnings("unchecked") // The cast has to work, because the constructor checks, that the return types match
    @Override
    public ReturnType invoke(Object instance, Object... parameters) {
        try {
            return (ReturnType) method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            getLogger().log(Level.FINER, "Error invoking the Function " + getShortMethodName(), e);
        }
        return null;
    }

    @Override
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        if (getMessageKey() == null || getMessageKey().isEmpty()) {
            return getShortMethodName();
        }
        return stringMessages.get(locale, getMessageKey());
    }
    
    private String getShortMethodName() {
        return method.getName();
    }

    private String getMessageKey() {
        return messageKey;
    }

    @Override
    public String toString() {
        return getDeclaringClass().getSimpleName() + "." + getShortMethodName();
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
