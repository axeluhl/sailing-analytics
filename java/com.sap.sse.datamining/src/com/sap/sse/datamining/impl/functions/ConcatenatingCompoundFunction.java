package com.sap.sse.datamining.impl.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class ConcatenatingCompoundFunction<ReturnType> extends AbstractFunction<ReturnType> {

    private static final String SIMPLE_NAME_CHAIN_CONNECTOR = " -> ";
    private static final String LOCALIZED_NAME_CHAIN_CONNECTOR = " ";
    
    private final String name;
    private final List<Function<?>> functions;
    private final int ordinal;

    public ConcatenatingCompoundFunction(String name, List<Function<?>> functions, Class<ReturnType> returnType) throws IllegalArgumentException {
        super(isLastFunctionADimension(functions));
        checkThatReturnTypesMatch(functions, returnType);
        
        this.name = name;
        this.functions = new ArrayList<>(functions);
        this.ordinal = calculateOrdinal();
    }

    private int calculateOrdinal() {
        int ordinal = Integer.MAX_VALUE;
        for (Function<?> function : functions) {
            int functionOrdinal = function.getOrdinal();
            if (functionOrdinal < ordinal) {
                ordinal = functionOrdinal;
            }
        }
        return ordinal;
    }

    private static boolean isLastFunctionADimension(List<Function<?>> functions) {
        return functions.get(functions.size() - 1).isDimension();
    }

    private void checkThatReturnTypesMatch(List<Function<?>> functions, Class<ReturnType> returnType) {
        Class<?> lastFunctionReturnType = functions.get(functions.size() - 1).getReturnType();
        if (!lastFunctionReturnType.equals(returnType)) {
            throw new IllegalArgumentException("The method return type " + lastFunctionReturnType.getName()
                    + " and expected return type " + returnType.getName() + " don't match");
        }
    }

    @Override
    public Class<?> getDeclaringType() {
        return getFirstFunction().getDeclaringType();
    }

    @Override
    public Iterable<Class<?>> getParameters() {
        return getFirstFunction().getParameters();
    }
    
    @SuppressWarnings("unchecked") // The cast has to work. The types were checked in the constructor.
    @Override
    public Class<ReturnType> getReturnType() {
        return (Class<ReturnType>) getLastFunction().getReturnType();
    }
    
    @Override
    public String getSimpleName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        
        return buildSimpleNameChain();
    }

    private String buildSimpleNameChain() {
        Iterator<Function<?>> functionsIterator = functions.iterator();
        StringBuilder builder = new StringBuilder(functionsIterator.next().getSimpleName());
        while (functionsIterator.hasNext()) {
            Function<?> function = functionsIterator.next();
            builder.append(SIMPLE_NAME_CHAIN_CONNECTOR + function.getSimpleName());
        }
        return builder.toString();
    }

    @Override
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages) {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (!isLocatable()) {
            return getSimpleName();
        }
        
        return buildLocalizedNameChain(locale, stringMessages);
    }
    
    @Override
    public boolean isLocatable() {
        for (Function<?> function : functions) {
            if (function.isLocatable()) {
                return true;
            }
        }
        return false;
    }

    private String buildLocalizedNameChain(Locale locale, ResourceBundleStringMessages stringMessages) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Function<?> function : functions) {
            if (function.isLocatable()) {
                if (!first) {
                    builder.append(LOCALIZED_NAME_CHAIN_CONNECTOR);
                }
                builder.append(function.getLocalizedName(locale, stringMessages));
                first = false;
            }
        }
        return builder.toString();
    }

    @Override
    public ReturnType tryToInvoke(Object instance) {
        return tryToInvoke(instance, new Object[0]);
    }

    @SuppressWarnings("unchecked") // The cast has to work. The types were checked in the constructor.
    @Override
    public ReturnType tryToInvoke(Object instance, Object... parameters) {
        Iterator<Function<?>> functionsIterator = functions.iterator();
        Object result = functionsIterator.next().tryToInvoke(instance, parameters);
        while (functionsIterator.hasNext()) {
            Function<?> function = (Function<?>) functionsIterator.next();
            result = function.tryToInvoke(result);
            if (result == null) {
                return null;
            }
        }
        return (ReturnType) result;
    }

    @Override
    public Unit getResultUnit() {
        return getLastFunction().getResultUnit();
    }

    @Override
    public int getResultDecimals() {
        return getLastFunction().getResultDecimals();
    }
    
    @Override
    public int getOrdinal() {
        return ordinal;
    }
    
    public List<Function<?>> getFunctions() {
        return functions;
    }

    private Function<?> getFirstFunction() {
        return functions.get(0);
    }

    private Function<?> getLastFunction() {
        return functions.get(functions.size() - 1);
    }
    
    @Override
    public String toString() {
        return getSimpleName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((functions == null) ? 0 : functions.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ConcatenatingCompoundFunction<?> other = (ConcatenatingCompoundFunction<?>) obj;
        if (functions == null) {
            if (other.functions != null)
                return false;
        } else if (!functions.equals(other.functions))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
}
