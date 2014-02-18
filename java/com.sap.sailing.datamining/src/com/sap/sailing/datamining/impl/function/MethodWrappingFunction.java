package com.sap.sailing.datamining.impl.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.SideEffectFreeValue;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTOImpl;

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
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        if (getMessageKey() == null || getMessageKey().isEmpty()) {
            return getMethodName();
        }
        return stringMessages.get(locale, getMessageKey());
    }
    
    private String getMethodName() {
        return method.getName();
    }

    private String getMessageKey() {
        return messageKey;
    }
    
    @Override
    public FunctionDTO asDTO() {
        return createDTO(getMethodName());
    }

    @Override
    public FunctionDTO asDTO(Locale locale, DataMiningStringMessages stringMessages) {
        return createDTO(getLocalizedName(locale, stringMessages));
    }
    
    private FunctionDTO createDTO(String displayName) {
        String functionName = getMethodName();
        String sourceTypeName = getDeclaringClass().getSimpleName();
        String returnTypeName = method.getReturnType().getSimpleName();
        List<String> parameterTypeNames = getParameterTypeNames();
        return new FunctionDTOImpl(functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, isDimension());
    }

    private List<String> getParameterTypeNames() {
        List<String> parameterTypeNames = new ArrayList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            parameterTypeNames.add(parameterType.getSimpleName());
        }
        return parameterTypeNames;
    }

    @Override
    public String toString() {
        return getDeclaringClass().getSimpleName() + "." + getMethodName() + "(" + parametersAsString() + ") : " + method.getReturnType().getSimpleName();
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
