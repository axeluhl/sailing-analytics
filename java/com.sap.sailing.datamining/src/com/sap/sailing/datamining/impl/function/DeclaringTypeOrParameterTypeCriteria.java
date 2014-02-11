package com.sap.sailing.datamining.impl.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.function.Function;
import com.sap.sse.datamining.components.FilterCriteria;

public class DeclaringTypeOrParameterTypeCriteria implements FilterCriteria<Function<?>> {

    private final Collection<Class<?>> expectingTypes;

    public DeclaringTypeOrParameterTypeCriteria(Class<?> expectingType) {
        this.expectingTypes = getSupertypesOf(expectingType);
        this.expectingTypes.add(expectingType);
    }

    private Collection<Class<?>> getSupertypesOf(Class<?> type) {
        Collection<Class<?>> supertypes = new HashSet<>();
        
        boolean supertypeAdded = supertypes.addAll(getInterfacesOf(type));
        if (isSuperclassValid(type)) {
            boolean superclassAdded = supertypes.add(type.getSuperclass());
            supertypeAdded = supertypeAdded ? true : superclassAdded;
        }
        
        do {
            Collection<Class<?>> supertypesToAdd = new HashSet<>();
            for (Class<?> supertype : supertypes) {
                supertypesToAdd.addAll(getSupertypesOf(supertype));
            }
            supertypeAdded = supertypes.addAll(supertypesToAdd);
        } while (supertypeAdded);
        
        return supertypes;
    }

    private boolean isSuperclassValid(Class<?> type) {
        return type.getSuperclass() != null && !type.getSuperclass().equals(Object.class);
    }

    private Collection<Class<?>> getInterfacesOf(Class<?> type) {
        return Arrays.asList(type.getInterfaces());
    }

    @Override
    public boolean matches(Function<?> function) {
        for (Class<?> expectingType : expectingTypes) {
            if (isDeclaringTypeMatching(function, expectingType) ||
                isAParameterTypeMatching(function, expectingType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAParameterTypeMatching(Function<?> function, Class<?> expectingType) {
        for (Class<?> parameterType : function.getParameters()) {
            if (parameterType.equals(expectingType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeclaringTypeMatching(Function<?> function, Class<?> expectingType) {
        return function.getDeclaringClass().equals(expectingType);
    }

}
