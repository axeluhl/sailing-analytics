package com.sap.sse.datamining.impl.functions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class RegistryFunctionsProvider implements FunctionProvider {
    
    private static final Logger LOGGER = Logger.getLogger(RegistryFunctionsProvider.class.getName());

    private FunctionRegistry functionRegistry;

    public RegistryFunctionsProvider(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
    
    @Override
    public Collection<Function<?>> getTransitiveDimensionsFor(Class<?> dataType, int depth) {
        Collection<Function<?>> dimensions = new HashSet<>();

        Collection<Class<?>> typesToCheck = new HashSet<>();
        Collection<Class<?>> typesToAdd = new HashSet<>();
        typesToCheck.add(dataType);
        for (int i = 0; i <= depth; i++) {
            for (Class<?> type : typesToCheck) {
                dimensions.addAll(getDimensionsFor(type));
                typesToAdd.addAll(getReturnTypesOfFunctionsFor(type));
            }
            typesToCheck.clear();
            typesToCheck.addAll(typesToAdd);
            typesToAdd.clear();
        }
        return dimensions;
    }
    
    private Collection<Class<?>> getReturnTypesOfFunctionsFor(Class<?> type) {
        Collection<Class<?>> returnTypes = new HashSet<>();
        for (Function<?> function : getFunctionsFor(type)) {
            returnTypes.add(function.getReturnType());
        }
        return returnTypes;
    }

    @Override
    public Collection<Function<?>> getDimensionsFor(Class<?> sourceType) {
        Collection<Class<?>> typesToRetrieve = getSupertypesOf(sourceType);
        typesToRetrieve.add(sourceType);
        return getDimensionsFor(typesToRetrieve);
    }

    private Collection<Function<?>> getDimensionsFor(Collection<Class<?>> typesToRetrieve) {
        Collection<Function<?>> dimensions = new HashSet<>();
        for (Class<?> typeToRetrieve : typesToRetrieve) {
            dimensions.addAll(functionRegistry.getDimensionsOf(typeToRetrieve));
        }
        return dimensions;
    }

    @Override
    public Collection<Function<?>> getFunctionsFor(Class<?> sourceType) {
        Collection<Class<?>> typesToRetrieve = getSupertypesOf(sourceType);
        typesToRetrieve.add(sourceType);
        return getFunctionsFor(typesToRetrieve);
    }
    
    private Collection<Function<?>> getFunctionsFor(Collection<Class<?>> typesToRetrieve) {
        Collection<Function<?>> dimensions = new HashSet<>();
        for (Class<?> typeToRetrieve : typesToRetrieve) {
            dimensions.addAll(functionRegistry.getFunctionsOf(typeToRetrieve));
        }
        return dimensions;
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
        FilterCriteria<Function<?>> functionDTOFilterCriteria = new FunctionMatchesDTOFilterCriteria(functionDTO);
        for (Function<?> function : functionRegistry.getAllFunctions()) {
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

}
