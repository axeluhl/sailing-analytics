package com.sap.sse.datamining.impl.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * Provides the {@link Function} interface for a list of functions in a way that the functions are concatenated
 * (like <code>foo().bar().value();</code>).
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <ReturnType> the return type of the compound function
 */
public class ConcatenatingCompoundFunction<ReturnType> extends AbstractFunction<ReturnType> {

    private static final String SIMPLE_NAME_CHAIN_CONNECTOR = ".";
    private static final String LOCALIZED_NAME_CHAIN_CONNECTOR = " ";
    
    private final List<Function<?>> functions;
    
    private final String simpleName;
    private final int ordinal;

    public ConcatenatingCompoundFunction(List<Function<?>> functions, Class<ReturnType> returnType) throws IllegalArgumentException {
        super(isLastFunctionADimension(functions));
        checkThatReturnTypesMatch(functions, returnType);
        
        this.functions = new ArrayList<>(functions);
        
        simpleName = buildSimpleName();
        ordinal = calculateOrdinal();
    }

    private String buildSimpleName() {
        Iterator<Function<?>> functionsIterator = functions.iterator();
        StringBuilder builder = new StringBuilder(functionsIterator.next().getSimpleName());
        while (functionsIterator.hasNext()) {
            Function<?> function = functionsIterator.next();
            builder.append(SIMPLE_NAME_CHAIN_CONNECTOR + function.getSimpleName());
        }
        return builder.toString();
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

    /**
     * @return the declaring type of the first function.
     */
    @Override
    public Class<?> getDeclaringType() {
        return getFirstFunction().getDeclaringType();
    }


    /**
     * @return the parameters of the first function.
     */
    @Override
    public Iterable<Class<?>> getParameters() {
        return getFirstFunction().getParameters();
    }
    
    @Override
    public boolean needsLocalizationParameters() {
        for (final Function<?> function : getFunctions()) {
            if (function.needsLocalizationParameters()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the return type of the last function.
     */
    @SuppressWarnings("unchecked") // The cast has to work. The types were checked in the constructor.
    @Override
    public Class<ReturnType> getReturnType() {
        return (Class<ReturnType>) getLastFunction().getReturnType();
    }
    
    /**
     * @return the concatenated simple names of the functions separated by {@value #SIMPLE_NAME_CHAIN_CONNECTOR}.
     */
    @Override
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * The concatenated localized names of the functions separated by {@value #LOCALIZED_NAME_CHAIN_CONNECTOR}.
     */
    @Override
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages) {
        if (!isLocalizable()) {
            return getSimpleName();
        }
        
        return buildLocalizedNameChain(locale, stringMessages);
    }
    @Override
    public boolean isLocalizable() {
        for (Function<?> function : functions) {
            if (function.isLocalizable()) {
                return true;
            }
        }
        return false;
    }

    private String buildLocalizedNameChain(Locale locale, ResourceBundleStringMessages stringMessages) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Function<?> function : functions) {
            if (function.isLocalizable()) {
                if (!first) {
                    builder.append(LOCALIZED_NAME_CHAIN_CONNECTOR);
                }
                builder.append(function.getLocalizedName(locale, stringMessages));
                first = false;
            }
        }
        return builder.toString();
    }

    /**
     * Invokes the functions concatenated (like <code>foo().bar().value();</code>).<br>
     * Returns <code>null</code>, if any of the functions returned <code>null</code>.
     */
    @Override
    public ReturnType tryToInvoke(Object instance) {
        return tryToInvoke(instance, ParameterProvider.NULL);
    }

    /**
     * Invokes the functions concatenated (like <code>foo(parameters).bar().value();</code>).<br>
     * Returns <code>null</code>, if any of the functions returned <code>null</code>.
     */
    @Override
    public ReturnType tryToInvoke(Object instance, ParameterProvider parameterProvider) {
        Iterator<Function<?>> functionsIterator = functions.iterator();
        Object result = functionsIterator.next().tryToInvoke(instance, parameterProvider);
        while (functionsIterator.hasNext()) {
            Function<?> function = functionsIterator.next();
            result = function.tryToInvoke(result, parameterProvider);
            if (result == null) {
                return null;
            }
        }
        @SuppressWarnings("unchecked")
        ReturnType typedResult = (ReturnType) result;
        return typedResult;
    }

    /**
     * @return the result decimals of the last function.
     */
    @Override
    public int getResultDecimals() {
        return getLastFunction().getResultDecimals();
    }
    
    /**
     * @return the smallest ordinal of the functions.
     */
    @Override
    public int getOrdinal() {
        return ordinal;
    }
    
    public List<Function<?>> getFunctions() {
        return functions;
    }

    /**
     * @return The list of functions as simple functions (like {@link MethodWrappingFunction}).
     */
    public List<MethodWrappingFunction<?>> getSimpleFunctions() {
        List<MethodWrappingFunction<?>> simpleFunctions = new ArrayList<>();
        for (Function<?> function : functions) {
            if (function.getClass().equals(MethodWrappingFunction.class)) {
                simpleFunctions.add((MethodWrappingFunction<?>) function);
                continue;
            }
            if (function.getClass().equals(ConcatenatingCompoundFunction.class)) {
                simpleFunctions.addAll(((ConcatenatingCompoundFunction<?>) function).getSimpleFunctions());
                continue;
            }
            throw new IllegalArgumentException("Can't simplify functions of type " + function.getClass().getSimpleName());
        }
        return simpleFunctions;
    }

    private Function<?> getFirstFunction() {
        return functions.get(0);
    }

    private Function<?> getLastFunction() {
        return functions.get(functions.size() - 1);
    }
    
    @Override
    public String toString() {
        return getDeclaringType().getSimpleName() + "." + getSimpleName() + " : " + getReturnType().getSimpleName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((functions == null) ? 0 : getSimpleFunctions().hashCode());
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
        } else if (!getSimpleFunctions().equals(other.getSimpleFunctions()))
            return false;
        return true;
    }
    
}
