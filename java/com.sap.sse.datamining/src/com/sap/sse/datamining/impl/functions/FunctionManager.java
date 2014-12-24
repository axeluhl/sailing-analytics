package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.impl.functions.criterias.FunctionMatchesDTOFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidConnectorFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidDimensionFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidExternalFunctionFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidStatisticFilterCriterion;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class FunctionManager implements FunctionRegistry, FunctionProvider {
    
    private enum FunctionRetrievalStrategies {
        All {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionRegistry registry) {
                return registry.getAllFunctionsOf(declaringType);
            }
        },
        Dimensions {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionRegistry registry) {
                return registry.getDimensionsOf(declaringType);
            }
        },
        Statistics {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionRegistry registry) {
                return registry.getStatisticsOf(declaringType);
            }
        };
        
        public abstract Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionRegistry registry);
        
    }
    
    private static final Logger LOGGER = Logger.getLogger(FunctionManager.class.getName());

    private final FilterCriterion<Method> isValidDimension = new MethodIsValidDimensionFilterCriterion();
    private final FilterCriterion<Method> isValidStatistic = new MethodIsValidStatisticFilterCriterion();
    private final FilterCriterion<Method> isValidConnector = new MethodIsValidConnectorFilterCriterion();
    private final FilterCriterion<Method> isValidExternalFunction = new MethodIsValidExternalFunctionFilterCriterion();

    private final FunctionFactory functionFactory;
    private final FunctionDTOFactory functionDTOFactory;
    
    private final Map<Class<?>, Set<Function<?>>> statistics;
    private final Map<Class<?>, Set<Function<?>>> dimensions;
    private final Map<Class<?>, Set<Function<?>>> externalFunctions;
    
    private final Collection<Map<Class<?>, Set<Function<?>>>> functionMaps;
    
    private final Collection<ParameterProvider> parameterProviders;

    public FunctionManager() {
        functionDTOFactory = new FunctionDTOFactory();
        functionFactory = new FunctionFactory();
        
        statistics = new HashMap<>();
        dimensions = new HashMap<>();
        externalFunctions = new HashMap<>();
        
        functionMaps = new ArrayList<>();
        functionMaps.add(statistics);
        functionMaps.add(dimensions);
        functionMaps.add(externalFunctions);
        
        parameterProviders = new HashSet<>();
    }
    
    @Override
    public void registerAllWithInternalFunctionPolicy(Iterable<Class<?>> internalClassesToScan) {
        for (Class<?> internalClass : internalClassesToScan) {
            scanInternalClass(internalClass);
        }
    }
    
    private void scanInternalClass(Class<?> internalClass) {
        scanInternalClass(internalClass, new ArrayList<Function<?>>());
    }

    private void scanInternalClass(Class<?> internalClass, List<Function<?>> previousFunctions) {
        for (Method method : internalClass.getMethods()) {
            if (isValidDimension.matches(method) || isValidStatistic.matches(method)) {
                registerFunction(previousFunctions, method);
                continue;
            }
            
            if (isValidConnector.matches(method)) {
                handleConnectorMethod(method, previousFunctions);
            }
        }
    }

    private void registerFunction(List<Function<?>> previousFunctions, Method method) {
        Function<?> function = functionFactory.createMethodWrappingFunction(method);
        if (!previousFunctions.isEmpty()) {
            function = functionFactory.createCompoundFunction(null, previousFunctions, function);
        }
        
        if (function.isDimension()) {
            addDimension(function);
        } else {
            addStatistic(function);
        }
    }

    private void addDimension(Function<?> dimension) {
        Class<?> declaringType = dimension.getDeclaringType();
        if (!dimensions.containsKey(declaringType)) {
            dimensions.put(declaringType, new HashSet<Function<?>>());
        }
        dimensions.get(declaringType).add(dimension);
    }

    private void addStatistic(Function<?> statistic) {
        Class<?> declaringType = statistic.getDeclaringType();
        if (!statistics.containsKey(declaringType)) {
            statistics.put(declaringType, new HashSet<Function<?>>());
        }
        statistics.get(declaringType).add(statistic);
    }

    private void handleConnectorMethod(Method method, List<Function<?>> previousFunctions) {
        Function<?> function = functionFactory.createMethodWrappingFunction(method);
        Class<?> returnType = method.getReturnType();
        List<Function<?>> previousFunctionsClone = new ArrayList<>(previousFunctions);
        previousFunctionsClone.add(function);
        scanInternalClass(returnType, previousFunctionsClone); 
    }

    @Override
    public void registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan) {
        for (Class<?> externalClass : externalClassesToScan) {
            for (Method method : externalClass.getMethods()) {
                if (isValidExternalFunction.matches(method)) {
                    Function<?> function = functionFactory.createMethodWrappingFunction(method);
                    addExternalFunction(function);
                }
            }
        }
    }
    
    private void addExternalFunction(Function<?> function) {
        Class<?> declaringType = function.getDeclaringType();
        if (!externalFunctions.containsKey(declaringType)) {
            externalFunctions.put(declaringType, new HashSet<Function<?>>());
        }
        externalFunctions.get(declaringType).add(function);
    }
    
    @Override
    public void registerParameterProvider(ParameterProvider parameterProvider) {
        parameterProviders.add(parameterProvider);
    }

    @Override
    public void unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister) {
        for (Class<?> classToUnregister : classesToUnregister) {
            unregisterAllFunctionsOf(classToUnregister);
        }
    }

    private void unregisterAllFunctionsOf(Class<?> classToUnregister) {
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            functionMap.remove(classToUnregister);
        }
    }
    
    @Override
    public void unregisterParameterProvider(ParameterProvider parameterProvider) {
        parameterProviders.remove(parameterProvider);
    }

    @Override
    public Collection<Function<?>> getAllFunctions() {
        Collection<Function<?>> allFunctions = new HashSet<>();
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            allFunctions.addAll(asSet(functionMap));
        }
        return allFunctions;
    }

    @Override
    public Collection<Function<?>> getAllFunctionsOf(Class<?> declaringType) {
        Collection<Function<?>> allFunctions = new HashSet<>();
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            Collection<Function<?>> functions = functionMap.get(declaringType);
            if (functions != null) {
                allFunctions.addAll(functions);
            }
        }
        return allFunctions;
    }

    @Override
    public Collection<Function<?>> getStatistics() {
        return asSet(statistics);
    }
    
    @Override
    public Collection<Function<?>> getStatisticsOf(Class<?> declaringType) {
        return statistics.get(declaringType);
    }
    
    @Override
    public Collection<Function<?>> getDimensions() {
        return asSet(dimensions);
    }
    
    @Override
    public Collection<Function<?>> getDimensionsOf(Class<?> declaringType) {
        return dimensions.get(declaringType);
    }
    
    @Override
    public Collection<Function<?>> getExternalFunctions() {
        return asSet(externalFunctions);
    }

    @Override
    public Collection<Function<?>> getExternalFunctionsOf(Class<?> declaringType) {
        return externalFunctions.get(declaringType);
    }
    
    private Collection<Function<?>> asSet(Map<?, Set<Function<?>>> map) {
        Collection<Function<?>> set = new HashSet<>();
        for (Entry<?, Set<Function<?>>> entry : map.entrySet()) {
            set.addAll(entry.getValue());
        }
        return set;
    }
    
    @Override
    public Iterable<ParameterProvider> getAllParameterProviders() {
        return parameterProviders;
    }
    
    @Override
    public Collection<Function<?>> getAllStatistics() {
        return getStatistics();
    }

    @Override
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType) {
        return getFunctionsFor(sourceType, FunctionRetrievalStrategies.All);
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType) {
        return getFunctionsFor(sourceType, FunctionRetrievalStrategies.Dimensions);
    }
    
    @Override
    public Collection<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        Collection<Function<?>> dimensions = new HashSet<>();
        for (DataRetrieverTypeWithInformation<?, ?> dataRetrieverTypeWithInformation : dataRetrieverChainDefinition.getDataRetrieverTypesWithInformation()) {
            dimensions.addAll(getDimensionsFor(dataRetrieverTypeWithInformation.getRetrievedDataType()));
        }
        return dimensions;
    }
    
    @Override
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType) {
        return getFunctionsFor(sourceType, FunctionRetrievalStrategies.Statistics);
    }

    private Collection<Function<?>> getFunctionsFor(Class<?> sourceType, FunctionRetrievalStrategies retrievalStrategy) {
        Collection<Class<?>> typesToRetrieve = getSupertypesOf(sourceType);
        typesToRetrieve.add(sourceType);
        return getFunctionsFor(typesToRetrieve, retrievalStrategy);
    }

    private Collection<Function<?>> getFunctionsFor(Collection<Class<?>> typesToRetrieve, FunctionRetrievalStrategies retrievalStrategy) {
        Collection<Function<?>> functions = new HashSet<>();
        for (Class<?> typeToRetrieve : typesToRetrieve) {
            Collection<Function<?>> typeSpecificFunctions = retrievalStrategy.retrieveFunctions(typeToRetrieve, this);
            if (typeSpecificFunctions != null) {
                functions.addAll(typeSpecificFunctions);
            }
        }
        return functions;
    }

    private Collection<Class<?>> getSupertypesOf(Class<?> type) {
        Collection<Class<?>> supertypes = new HashSet<>();
        
        supertypes.addAll(getInterfacesOf(type));
        if (isSuperclassValid(type)) {
            supertypes.add(type.getSuperclass());
        }
        
        supertypes.addAll(getSupertypesOf(supertypes));
        return supertypes;
    }

    private Collection<Class<?>> getInterfacesOf(Class<?> type) {
        return Arrays.asList(type.getInterfaces());
    }

    private boolean isSuperclassValid(Class<?> subType) {
        return subType.getSuperclass() != null && !subType.getSuperclass().equals(Object.class);
    }

    private Collection<Class<?>> getSupertypesOf(Collection<Class<?>> types) {
        Collection<Class<?>> supertypes = new HashSet<>();
        boolean supertypeAdded;
        do {
            Collection<Class<?>> supertypesToAdd = getSupertypesToAdd(types);
            supertypeAdded = supertypes.addAll(supertypesToAdd);
        } while (supertypeAdded);
        return supertypes;
    }

    private Collection<Class<?>> getSupertypesToAdd(Collection<Class<?>> types) {
        Collection<Class<?>> supertypesToAdd = new HashSet<>();
        for (Class<?> supertype : types) {
            supertypesToAdd.addAll(getSupertypesOf(supertype));
        }
        return supertypesToAdd;
    }
    
    @Override
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO) {
        if (functionDTO == null) {
            return null;
        }
        
        Collection<Function<?>> functionsMatchingDTO = getFunctionsForDTO(functionDTO);
        if (moreThanOneFunctionMatchedDTO(functionsMatchingDTO)) {
            logThatMoreThanOneFunctionMatchedDTO(functionDTO, functionsMatchingDTO);
        }
        
        return getFunctionToReturn(functionsMatchingDTO);
    }

    private Collection<Function<?>> getFunctionsForDTO(FunctionDTO functionDTO) {
        Collection<Function<?>> functionsMatchingDTO = new HashSet<>();
        FilterCriterion<Function<?>> functionDTOFilterCriteria = new FunctionMatchesDTOFilterCriterion(functionDTOFactory, functionDTO);
        for (Function<?> function : getAllFunctions()) {
            if (functionDTOFilterCriteria.matches(function)) {
                functionsMatchingDTO.add(function);
            }
        }
        return functionsMatchingDTO;
    }

    private boolean moreThanOneFunctionMatchedDTO(Collection<Function<?>> functionsMatchingDTO) {
        return functionsMatchingDTO.size() > 1;
    }

    private void logThatMoreThanOneFunctionMatchedDTO(FunctionDTO functionDTO, Collection<Function<?>> functionsMatchingDTO) {
        LOGGER.log(Level.FINER, "More than on registered function matched the function DTO '" + functionDTO.toString() + "'");
        for (Function<?> function : functionsMatchingDTO) {
            LOGGER.log(Level.FINEST, "The function '" + function.toString() + "' matched the function DTO '" + functionDTO.toString() + "'");
        }
    }

    private Function<?> getFunctionToReturn(Collection<Function<?>> functionsMatchingDTO) {
        return functionsMatchingDTO.isEmpty() ? null : functionsMatchingDTO.iterator().next();
    }
    
    @Override
    public ParameterProvider getParameterProviderFor(Function<?> function) {
        Iterable<Class<?>> functionParameterTypes = function.getParameters();
        if (!functionParameterTypes.iterator().hasNext()) {
            return ParameterProvider.NULL;
        }
        
        for (ParameterProvider parameterProvider : getAllParameterProviders()) {
            if (parametersAreMatching(function, parameterProvider)) {
                return parameterProvider;
            }
        }
        
        return null;
    }

    private boolean parametersAreMatching(Function<?> function, ParameterProvider parameterProvider) {
        Iterator<Class<?>> functionParametersIterator = function.getParameters().iterator();
        Iterator<Class<?>> parameterProviderIterator = parameterProvider.getParameterTypes().iterator();
        
        while (functionParametersIterator.hasNext() && parameterProviderIterator.hasNext()) {
            Class<?> functionParameter = functionParametersIterator.next();
            Class<?> parameterProviderParameter = parameterProviderIterator.next();
            if (!functionParameter.isAssignableFrom(parameterProviderParameter)) {
                return false;
            }
        }
        
        if (functionParametersIterator.hasNext() || parameterProviderIterator.hasNext()) {
            return false;
        }
        
        return true;
    }

}
