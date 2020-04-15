package com.sap.sse.datamining.impl.components.management;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.management.FunctionProvider;
import com.sap.sse.datamining.components.management.FunctionRegistry;
import com.sap.sse.datamining.exceptions.MultipleDataMiningComponentsFoundForDTOException;
import com.sap.sse.datamining.factories.DataMiningDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.impl.functions.ConcatenatingCompoundFunction;
import com.sap.sse.datamining.impl.functions.IdentityFunction;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidConnectorFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidDimensionFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidExternalFunctionFilterCriterion;
import com.sap.sse.datamining.impl.functions.criterias.MethodIsValidStatisticFilterCriterion;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.util.ClassUtils;

public class FunctionManager implements FunctionRegistry, FunctionProvider {
    
    private enum FunctionRetrievalStrategies {
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
        },
        ExternalFunctions {
            @Override
            public Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionManager manager) {
                return manager.getExternalFunctionsOf(declaringType);
            }
        };
        
        public abstract Collection<Function<?>> retrieveFunctions(Class<?> declaringType, FunctionManager manager);
        
    }
    
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final FilterCriterion<Method> isValidDimension = new MethodIsValidDimensionFilterCriterion();
    private final FilterCriterion<Method> isValidStatistic = new MethodIsValidStatisticFilterCriterion();
    private final FilterCriterion<Method> isValidConnector = new MethodIsValidConnectorFilterCriterion();
    private final FilterCriterion<Method> isValidExternalFunction = new MethodIsValidExternalFunctionFilterCriterion();

    private final FunctionFactory functionFactory;
    
    protected final IdentityFunction identityFunction;
    protected final FunctionDTO identityFunctionDTO;
    
    protected final Map<Class<?>, Set<Function<?>>> statistics;
    protected final Map<Class<?>, Set<Function<?>>> dimensions;
    protected final Map<Class<?>, Set<Function<?>>> externalFunctions;
    
    private final Collection<Map<Class<?>, Set<Function<?>>>> functionMaps;

    public FunctionManager() {
        functionFactory = new FunctionFactory();
        
        identityFunction = new IdentityFunction();
        identityFunctionDTO = new DataMiningDTOFactory().createFunctionDTO(identityFunction);
        
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
            logger.info("Registering functions of class " + internalClass.getName() + " with internal policy");
            boolean functionsOfClassHaveBeenRegistered = scanInternalClass(internalClass);
            functionsHaveBeenRegistered = functionsHaveBeenRegistered ? true : functionsOfClassHaveBeenRegistered;
            if (functionsOfClassHaveBeenRegistered) {
                logger.info("Finished the registration of class " + internalClass.getName() + " with internal policy");
            } else {
                logger.info("Couldn't find any functions for class " + internalClass.getName() + " with internal policy");
            }
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
            }
            if (isValidConnector.matches(method)) {
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
        logger.finer("Registering dimension " + dimension + " for the internal class " + declaringType.getName());
        return dimensions.get(declaringType).add(dimension);
    }

    private boolean addStatistic(Function<?> statistic) {
        Class<?> declaringType = statistic.getDeclaringType();
        if (!statistics.containsKey(declaringType)) {
            statistics.put(declaringType, new HashSet<Function<?>>());
        }
        logger.finer("Registering statistic " + statistic + " for the internal class " + declaringType.getName());
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
            logger.info("Registering functions of class " + externalClass.getName() + " with external policy");
            for (Method method : externalClass.getMethods()) {
                if (isValidExternalFunction.matches(method)) {
                    Function<?> function = functionFactory.createMethodWrappingFunction(method);
                    boolean functionsOfClassHaveBeenRegistered = addExternalFunction(function);
                    functionsHaveBeenRegistered = functionsHaveBeenRegistered ? true : functionsOfClassHaveBeenRegistered;
                }
            }
            if (functionsHaveBeenRegistered) {
                logger.info("Finished the registration of class " + externalClass.getName() + " with external policy");
            } else {
                logger.info("Couldn't find any functions for class " + externalClass.getName() + " with external policy");
            }
        }
        return functionsHaveBeenRegistered;
    }
    
    private boolean addExternalFunction(Function<?> function) {
        Class<?> declaringType = function.getDeclaringType();
        if (!externalFunctions.containsKey(declaringType)) {
            externalFunctions.put(declaringType, new HashSet<Function<?>>());
        }
        logger.finer("Registering external function " + function + " for the external class " + declaringType.getName());
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
    
    @Override
    public IdentityFunction getIdentityFunction() {
        return identityFunction;
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
    
    private Collection<Function<?>> getExternalFunctionsOf(Class<?> declaringType) {
        return externalFunctions.get(declaringType);
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
        return getFunctionsFor(sourceType, Arrays.asList(FunctionRetrievalStrategies.values()));
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType) {
        return getFunctionsFor(sourceType, Collections.singleton(FunctionRetrievalStrategies.Dimensions));
    }

    @Override
    public Collection<Function<?>> getStatisticsFor(Class<?> sourceType) {
        return getFunctionsFor(sourceType, Collections.singleton(FunctionRetrievalStrategies.Statistics));
    }
    
    @Override
    public Collection<Function<?>> getExternalFunctionsFor(Class<?> sourceType) {
        return getFunctionsFor(sourceType, Collections.singleton(FunctionRetrievalStrategies.ExternalFunctions));
    }

    private Collection<Function<?>> getFunctionsFor(Class<?> sourceType, Iterable<FunctionRetrievalStrategies> retrievalStrategies) {
        Collection<Class<?>> typesToRetrieve = ClassUtils.getSupertypesOf(sourceType);
        typesToRetrieve.remove(Object.class);
        typesToRetrieve.add(sourceType);
        return getFunctionsFor(typesToRetrieve, retrievalStrategies);
    }

    private Collection<Function<?>> getFunctionsFor(Collection<Class<?>> typesToRetrieve, Iterable<FunctionRetrievalStrategies> retrievalStrategies) {
        Collection<Function<?>> functions = new HashSet<>();
        for (Class<?> typeToRetrieve : typesToRetrieve) {
            for (FunctionRetrievalStrategies retrievalStrategy : retrievalStrategies) {
                Collection<Function<?>> typeSpecificFunctions = retrievalStrategy.retrieveFunctions(typeToRetrieve, this);
                if (typeSpecificFunctions != null) {
                    functions.addAll(typeSpecificFunctions);
                }
            }
        }
        return functions;
    }
    
    @Override
    public Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getDimensionsMappedByLevelFor(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> dimensions = new HashMap<>();
        List<? extends DataRetrieverLevel<?, ?>> dataRetrieverLevels = dataRetrieverChainDefinition.getDataRetrieverLevels();
        for (DataRetrieverLevel<?, ?> dataRetrieverLevel : dataRetrieverLevels) {
            dimensions.put(dataRetrieverLevel, getDimensionsFor(dataRetrieverLevel.getRetrievedDataType()));
        }
        return dimensions;
    }
    
    @Override
    public ReducedDimensions getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> dimensionsMappedByLevel = getDimensionsMappedByLevelFor(dataRetrieverChainDefinition);
        List<? extends DataRetrieverLevel<?, ?>> retrieverLevels = dataRetrieverChainDefinition.getDataRetrieverLevels();
        ReducedDimensions reducedDimensions = new ReducedDimensions();
        for (DataRetrieverLevel<?, ?> retrieverLevel : retrieverLevels) {
            Iterable<Function<?>> dimensionsOfLevel = dimensionsMappedByLevel.get(retrieverLevel);
            DataRetrieverLevel<?, ?> previousRetrieverLevel = retrieverLevel.getLevel() > 0 ? retrieverLevels.get(retrieverLevel.getLevel() - 1) : null;
            final ReducedDimensions reducedDimensionsForLevel;
            if (previousRetrieverLevel == null) {
                assert reducedDimensions.getReducedDimensions().isEmpty();
                // for the first retriever level no reduction takes place; create a ReducedDimensions object
                // to "add" onto the empty ReducedDimensions object using the full set of dimensions of this level
                final Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> currentLevelToAllItsDimensions = new HashMap<>();
                final Map<Function<?>, Function<?>> fromOriginalToReducedDimension = new HashMap<>();
                currentLevelToAllItsDimensions.put(retrieverLevel, dimensionsOfLevel);
                for (final Function<?> dimension : dimensionsOfLevel) {
                    fromOriginalToReducedDimension.put(dimension, dimension);
                }
                reducedDimensionsForLevel = new ReducedDimensions(currentLevelToAllItsDimensions, fromOriginalToReducedDimension);
            } else {
                reducedDimensionsForLevel = reduce(dimensionsOfLevel, previousRetrieverLevel,
                        dimensionsMappedByLevel.get(previousRetrieverLevel), retrieverLevel);
            }
            reducedDimensions = reducedDimensions.createByAdd(reducedDimensionsForLevel,
                    /* replaceExistingMappingsFromOriginalToReducedDimension */ true);
        }
        return reducedDimensions;
    }
    
    private ReducedDimensions reduce(Iterable<Function<?>> dimensionsToReduce,
            DataRetrieverLevel<?, ?> previousRetrieverLevel, Iterable<Function<?>> previousDimensions,
            DataRetrieverLevel<?, ?> currentRetrieverLevel) {
        final Iterable<Function<?>> reducedDimensions;
        final Map<Function<?>, Function<?>> fromOriginalToReducedDimensions = new HashMap<>();
        if (Util.isEmpty(previousDimensions)) {
            reducedDimensions = dimensionsToReduce;
            for (final Function<?> dimensionToReduce : dimensionsToReduce) {
                fromOriginalToReducedDimensions.put(dimensionToReduce, dimensionToReduce);
            }
        } else {
            final Set<Function<?>> modifiableReducedDimensions = new HashSet<>();
            reducedDimensions = modifiableReducedDimensions;
            for (Function<?> dimension : dimensionsToReduce) {
                boolean isAllowed = true;
                if (ConcatenatingCompoundFunction.class.isAssignableFrom(dimension.getClass())) {
                    ConcatenatingCompoundFunction<?> compoundDimension = (ConcatenatingCompoundFunction<?>) dimension;
                    List<MethodWrappingFunction<?>> simpleFunctions = compoundDimension.getSimpleFunctions();
                    if (previousRetrieverLevel.getRetrievedDataType().isAssignableFrom(simpleFunctions.get(0).getReturnType())) {
                        List<MethodWrappingFunction<?>> subList = simpleFunctions.subList(1, simpleFunctions.size());
                        Function<?> subFunction = subList.size() == 1 ? subList.get(0)
                                                                            : functionFactory.createCompoundFunction(subList);
                        if (Util.contains(previousDimensions, subFunction)) {
                            isAllowed = false; // TODO record the reduction process, mapping original dimension to equal element from previousDimensions
                            for (final Function<?> previousDimension : previousDimensions) {
                                if (previousDimension.equals(subFunction)) {
                                    fromOriginalToReducedDimensions.put(dimension, previousDimension);
                                    break;
                                }
                            }
                        }
                    }
                }
                if (isAllowed) {
                    modifiableReducedDimensions.add(dimension);
                    fromOriginalToReducedDimensions.put(dimension, dimension);
                }
            }
        }
        final Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> reducedDimensionsPerRetrieverLevel = new HashMap<>();
        reducedDimensionsPerRetrieverLevel.put(currentRetrieverLevel, reducedDimensions);
        return new ReducedDimensions(reducedDimensionsPerRetrieverLevel, fromOriginalToReducedDimensions);
    }
    
    @Override
    public Function<?> getFunctionForDTO(FunctionDTO functionDTO, ClassLoader classLoader) {
        if (identityFunctionDTO.equals(functionDTO)) {
            return identityFunction;
        }
        
        Function<?> function = null;
        if (functionDTO != null) {
            try {
                Class<?> sourceType = ClassUtils.getClassForName(functionDTO.getSourceTypeName(), true, classLoader);
                Collection<Function<?>> possibleFunctions;
                if (functionDTO.isDimension()) {
                    possibleFunctions = getDimensionsFor(sourceType);
                } else {
                    possibleFunctions = getFunctionsFor(sourceType, Arrays.asList(FunctionRetrievalStrategies.ExternalFunctions, FunctionRetrievalStrategies.Statistics));
                }
                
                if (!possibleFunctions.isEmpty()) {
                    Class<?> returnType = ClassUtils.getClassForName(functionDTO.getReturnTypeName(), true, classLoader);
                    Set<Function<?>> matchingFunctions = new HashSet<>();
                    for (Function<?> possibleFunction : possibleFunctions) {
                        if (returnType.equals(possibleFunction.getReturnType()) &&
                            functionDTO.getFunctionName().equals(possibleFunction.getSimpleName())) {
                            matchingFunctions.add(possibleFunction);
                        }
                    }

                    if (matchingFunctions.size() == 1) {
                        function = matchingFunctions.iterator().next();
                    } else if (matchingFunctions.size() > 1) {
                        throw new MultipleDataMiningComponentsFoundForDTOException(functionDTO, matchingFunctions);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Couldn't get classes for the function DTO " + functionDTO, e);
            }
        }
        return function;
    }

}
