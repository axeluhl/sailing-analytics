package com.sap.sse.datamining.impl.functions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.components.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.impl.functions.criterias.FunctionMatchesDTOFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidConnectorFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidDimensionFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidExternalFunctionFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidStatisticFilterCriterion;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class FunctionManager implements FunctionRegistry, FunctionProvider {
    
    private enum FunctionRetrievalStrategies {
        All {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionManager manager) {
                return manager.getAllFunctionsOf(declaringType);
            }
        },
        Dimensions {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionManager manager) {
                return manager.getDimensionsOf(declaringType);
            }
        },
        Statistics {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionManager manager) {
                return manager.getStatisticsOf(declaringType);
            }
        };
        
        public abstract Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionManager manager);
        
    }
    
    private static final Logger LOGGER = Logger.getLogger(FunctionManager.class.getName());

    private final FilterCriterion<Method> isValidDimension = new MethodIsValidDimensionFilterCriterion();
    private final FilterCriterion<Method> isValidStatistic = new MethodIsValidStatisticFilterCriterion();
    private final FilterCriterion<Method> isValidConnector = new MethodIsValidConnectorFilterCriterion();
    private final FilterCriterion<Method> isValidExternalFunction = new MethodIsValidExternalFunctionFilterCriterion();

    private final FunctionFactory functionFactory;
    private final FunctionDTOFactory functionDTOFactory;
    
    protected final Map<Class<?>, Set<Function<?>>> statistics;
    protected final Map<Class<?>, Set<Function<?>>> dimensions;
    protected final Map<Class<?>, Set<Function<?>>> externalFunctions;
    
    private final Collection<Map<Class<?>, Set<Function<?>>>> functionMaps;

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
    }
    
    @Override
    public boolean registerAllClasses(Iterable<Class<?>> internalClassesToScan) {
        boolean functionsHaveBeenRegistered = false;
        for (Class<?> internalClass : internalClassesToScan) {
            boolean functionsOfClassHaveBeenRegistered = scanInternalClass(internalClass);
            functionsHaveBeenRegistered = functionsHaveBeenRegistered ? true : functionsOfClassHaveBeenRegistered;
        }
        return functionsHaveBeenRegistered;
    }
    
    private boolean scanInternalClass(Class<?> internalClass) {
        return scanInternalClass(internalClass, new ArrayList<Function<?>>(), true);
    }

    private boolean scanInternalClass(Class<?> internalClass, List<Function<?>> previousFunctions, boolean scanForStatistics) {
        boolean functionsHaveBeenRegistered = false;
        for (Method method : internalClass.getMethods()) {
            boolean functionHasBeenRegistered = false;
            if (isValidDimension.matches(method) || (scanForStatistics && isValidStatistic.matches(method) )) {
                functionHasBeenRegistered = registerFunction(previousFunctions, method);
            } else if (isValidConnector.matches(method)) {
                functionHasBeenRegistered = handleConnectorMethod(method, previousFunctions, scanForStatistics);
            }
            functionsHaveBeenRegistered = functionsHaveBeenRegistered ? true : functionHasBeenRegistered;
        }
        return functionsHaveBeenRegistered;
    }

    private boolean registerFunction(List<Function<?>> previousFunctions, Method method) {
        Function<?> function = functionFactory.createMethodWrappingFunction(method);
        if (!previousFunctions.isEmpty()) {
            function = functionFactory.createCompoundFunction(previousFunctions, function);
        }
        
        if (function.isDimension()) {
            return addDimension(function);
        } else {
            return addStatistic(function);
        }
    }

    private boolean addDimension(Function<?> dimension) {
        Class<?> declaringType = dimension.getDeclaringType();
        if (!dimensions.containsKey(declaringType)) {
            dimensions.put(declaringType, new HashSet<Function<?>>());
        }
        return dimensions.get(declaringType).add(dimension);
    }

    private boolean addStatistic(Function<?> statistic) {
        Class<?> declaringType = statistic.getDeclaringType();
        if (!statistics.containsKey(declaringType)) {
            statistics.put(declaringType, new HashSet<Function<?>>());
        }
        return statistics.get(declaringType).add(statistic);
    }

    private boolean handleConnectorMethod(Method method, List<Function<?>> previousFunctions, boolean scanForStatistics) {
        Function<?> function = functionFactory.createMethodWrappingFunction(method);
        Class<?> returnType = method.getReturnType();
        List<Function<?>> previousFunctionsClone = new ArrayList<>(previousFunctions);
        previousFunctionsClone.add(function);
        return scanInternalClass(returnType, previousFunctionsClone, !scanForStatistics ? false : method.getAnnotation(Connector.class).scanForStatistics()); 
    }

    @Override
    public boolean registerAllWithExternalFunctionPolicy(Iterable<Class<?>> externalClassesToScan) {
        boolean functionsHaveBeenRegistered = false;
        for (Class<?> externalClass : externalClassesToScan) {
            for (Method method : externalClass.getMethods()) {
                if (isValidExternalFunction.matches(method)) {
                    Function<?> function = functionFactory.createMethodWrappingFunction(method);
                    boolean functionsOfClassHaveBeenRegistered = addExternalFunction(function);
                    functionsHaveBeenRegistered = functionsHaveBeenRegistered ? true : functionsOfClassHaveBeenRegistered;
                }
            }
        }
        return functionsHaveBeenRegistered;
    }
    
    private boolean addExternalFunction(Function<?> function) {
        Class<?> declaringType = function.getDeclaringType();
        if (!externalFunctions.containsKey(declaringType)) {
            externalFunctions.put(declaringType, new HashSet<Function<?>>());
        }
        return externalFunctions.get(declaringType).add(function);
    }

    @Override
    public boolean unregisterAllFunctionsOf(Iterable<Class<?>> classesToUnregister) {
        boolean functionsHaveBeenUnregistered = false;
        for (Class<?> classToUnregister : classesToUnregister) {
            boolean functionsOfClassHaveBeenUnregistered = unregisterAllFunctionsOf(classToUnregister);
            functionsHaveBeenUnregistered = functionsHaveBeenUnregistered ? true : functionsOfClassHaveBeenUnregistered;
        }
        return functionsHaveBeenUnregistered;
    }

    private boolean unregisterAllFunctionsOf(Class<?> classToUnregister) {
        boolean functionsHaveBeenUnregistered = false;
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            boolean functionsOfClassHaveBeenUnregistered = functionMap.remove(classToUnregister) != null;
            functionsHaveBeenUnregistered = functionsHaveBeenUnregistered ? true : functionsOfClassHaveBeenUnregistered;
        }
        return functionsHaveBeenUnregistered;
    }

    private Collection<Function<?>> getAllFunctionsOf(Class<?> declaringType) {
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
    public Collection<Function<?>> getAllStatistics() {
        return asSet(statistics);
    }
    
    private Collection<Function<?>> getStatisticsOf(Class<?> declaringType) {
        return statistics.get(declaringType);
    }
    
    private Collection<Function<?>> getDimensionsOf(Class<?> declaringType) {
        return dimensions.get(declaringType);
    }
    
    protected Collection<Function<?>> asSet(Map<?, Set<Function<?>>> map) {
        Collection<Function<?>> set = new HashSet<>();
        for (Entry<?, Set<Function<?>>> entry : map.entrySet()) {
            set.addAll(entry.getValue());
        }
        return set;
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
        
        Function<?> function = getFunctionToReturn(functionsMatchingDTO);
        if (function == null) {
            LOGGER.log(Level.WARNING, "No function found for the DTO: " + functionDTO);
        }
        return function;
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

    private Collection<Function<?>> getAllFunctions() {
        Collection<Function<?>> allFunctions = new HashSet<>();
        for (Map<Class<?>, Set<Function<?>>> functionMap : functionMaps) {
            allFunctions.addAll(asSet(functionMap));
        }
        return allFunctions;
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

}
