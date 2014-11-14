package com.sap.sse.datamining.impl.functions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.impl.functions.criterias.FunctionMatchesDTOFilterCriterion;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class RegistryFunctionProvider implements FunctionProvider {
    
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
    
    private static final Logger LOGGER = Logger.getLogger(RegistryFunctionProvider.class.getName());

    private final FunctionDTOFactory functionDTOFactory;
    private final Collection<FunctionRegistry> functionRegistries;

    public RegistryFunctionProvider(FunctionRegistry... functionRegistry) {
        this(Arrays.asList(functionRegistry));
    }
    
    public RegistryFunctionProvider(Collection<FunctionRegistry> functionRegistries) {
        functionDTOFactory = new FunctionDTOFactory();
        this.functionRegistries = new HashSet<>(functionRegistries);
    }
    
    @Override
    public Collection<Function<?>> getAllStatistics() {
        Collection<Function<?>> allStatistics = new HashSet<>();
        for (FunctionRegistry registry : functionRegistries) {
            allStatistics.addAll(registry.getStatistics());
        }
        return allStatistics;
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
    public Collection<Function<?>> getDimensionsFor(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
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
        for (FunctionRegistry functionRegistry : functionRegistries) {
            for (Class<?> typeToRetrieve : typesToRetrieve) {
                Collection<Function<?>> typeSpecificFunctions = retrievalStrategy.retrieveFunctions(typeToRetrieve, functionRegistry);
                if (typeSpecificFunctions != null) {
                    functions.addAll(typeSpecificFunctions);
                }
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
        for (FunctionRegistry functionRegistry : functionRegistries) {
            for (Function<?> function : functionRegistry.getAllFunctions()) {
                if (functionDTOFilterCriteria.matches(function)) {
                    functionsMatchingDTO.add(function);
                }
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

}
