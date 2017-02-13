package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class MethodWrappingFunction<ReturnType> extends AbstractFunction<ReturnType> {
    private final Method method;
    private final Class<ReturnType> returnType;
    private final AdditionalMethodWrappingFunctionData additionalData;
    private final String simpleName;

    /**
     * Throws an {@link IllegalArgumentException}, if the return type of the method and the given
     * <code>returnType</code> aren't equal.
     */
    public MethodWrappingFunction(Method method, Class<ReturnType> returnType) throws IllegalArgumentException {
        super(isMethodADimension(method));
        checkThatReturnTypesMatch(method, returnType);
        this.method = method;
        this.returnType = returnType;
        this.additionalData = initializeAdditionalData();
        final String parametersAsString = parametersAsString();
        final StringBuilder simpleNameBuilder = new StringBuilder(method.getName().length()+parametersAsString.length()+2);
        simpleNameBuilder.append(method.getName()).append('(').append(parametersAsString).append(')');
        this.simpleName = simpleNameBuilder.toString();
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

    private AdditionalMethodWrappingFunctionData initializeAdditionalData() {
        final AdditionalMethodWrappingFunctionData result;
        if (method.getAnnotation(Connector.class) != null) {
            Connector connectorData = method.getAnnotation(Connector.class);
            result = new AdditionalMethodWrappingFunctionData(connectorData.messageKey(), 0,
                    connectorData.ordinal());
        } else if (method.getAnnotation(Statistic.class) != null) {
            Statistic statisticData = method.getAnnotation(Statistic.class);
            result = new AdditionalMethodWrappingFunctionData(statisticData.messageKey(),
                    statisticData.resultDecimals(), statisticData.ordinal());
        } else  if (method.getAnnotation(Dimension.class) != null) {
            Dimension dimensionData = method.getAnnotation(Dimension.class);
            result = new AdditionalMethodWrappingFunctionData(dimensionData.messageKey(), 0,
                    dimensionData.ordinal());
        } else {
            result = new AdditionalMethodWrappingFunctionData("", 0, Integer.MAX_VALUE);
        }
        return result;
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
    public boolean needsLocalizationParameters() {
        Iterator<Class<?>> parameterTypesIterator = getParameters().iterator();
        int parameterCount = 0;
        while (parameterTypesIterator.hasNext()) {
            parameterCount++;
            parameterTypesIterator.next();
        }
        if (parameterCount != 2) {
            return false;
        }
        parameterTypesIterator = getParameters().iterator();
        Class<?> firstParameter = parameterTypesIterator.next();
        Class<?> secondParameter = parameterTypesIterator.next();
        return firstParameter.isAssignableFrom(Locale.class) && secondParameter.isAssignableFrom(ResourceBundleStringMessages.class);
    }

    @Override
    public ReturnType tryToInvoke(Object instance) {
        return tryToInvoke(instance, new Object[0]);
    }

    @Override
    public ReturnType tryToInvoke(Object instance, ParameterProvider parameterProvider) {
        if (method.getParameterCount() == 0) {
            return tryToInvoke(instance);
        } else {
            // copy only as many parameters as the function requires (see also bug 4034)
            final Object[] paramValues = new Object[method.getParameterCount()];
            System.arraycopy(parameterProvider.getParameters(), 0, paramValues, 0, paramValues.length);
            return tryToInvoke(instance, paramValues);
        }
    }

    @SuppressWarnings("unchecked")
    // The cast has to work, because the constructor checks, that the return types match
    private ReturnType tryToInvoke(Object instance, Object... parameters) {
        if (instance != null) {
            try {
                return (ReturnType) method.invoke(instance, parameters);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                getLogger().log(Level.FINER, "Error invoking the Function " + method.getName(), e);
            }
        }
        return null;
    }

    @Override
    public int getResultDecimals() {
        return additionalData.getResultDecimals();
    }

    @Override
    public int getOrdinal() {
        return additionalData.getOrdinal();
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    private String parametersAsString() {
        StringBuilder parameterBuilder = new StringBuilder();
        boolean first = true;
        for (Class<?> parameterType : getParameters()) {
            if (!first) {
                parameterBuilder.append(", ");
            }
            parameterBuilder.append(parameterType.getName());
            first = false;
        }
        return parameterBuilder.toString();
    }

    @Override
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages) {
        if (!isLocalizable()) {
            return getSimpleName();
        }
        return stringMessages.get(locale, additionalData.getMessageKey());
    }

    @Override
    public boolean isLocalizable() {
        return additionalData != null && (!additionalData.getMessageKey().isEmpty());
    }

    @Override
    public String toString() {
        return getDeclaringType().getSimpleName() + "." + method.getName() + "(" + parametersAsSimpleString() + ") : "
                + method.getReturnType().getSimpleName();
    }

    private String parametersAsSimpleString() {
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
