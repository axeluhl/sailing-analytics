package com.sap.sailing.datamining.function.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.function.Function;

public class DeclaringTypeOrParameterTypeCriteria implements ConcurrentFilterCriteria<Function> {

    private final Collection<Class<?>> expectingTypes;

    public DeclaringTypeOrParameterTypeCriteria(Class<?> expectingType) {
        this.expectingTypes = new HashSet<>();
        addAllSupertypesOf(expectingType);
        this.expectingTypes.add(expectingType);
    }

    private void addAllSupertypesOf(Class<?> type) {
        boolean wasSuperclassAdded = false;
        boolean wereInterfacesAdded = false;
        
        if (isSuperTypeValid(type)) {
            wasSuperclassAdded = expectingTypes.add(type.getSuperclass());
        }
        wereInterfacesAdded = expectingTypes.addAll(getInterfacesOf(type));
        
        if (wasSuperclassAdded || wereInterfacesAdded) {
            for (Class<?> expectingType : expectingTypes) {
                addAllSupertypesOf(expectingType);
            }
        }
    }

    public boolean isSuperTypeValid(Class<?> type) {
        return type.getSuperclass() != null && !type.getSuperclass().equals(Object.class);
    }

    private Collection<Class<?>> getInterfacesOf(Class<?> type) {
        return Arrays.asList(type.getInterfaces());
    }

    @Override
    public boolean matches(Function function) {
        for (Class<?> expectingType : expectingTypes) {
            if (isDeclaringTypeMatching(function, expectingType) ||
                isAParameterTypeMatching(function, expectingType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAParameterTypeMatching(Function function, Class<?> expectingType) {
        for (Class<?> parameterType : function.getParameters()) {
            if (parameterType.equals(expectingType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeclaringTypeMatching(Function function, Class<?> expectingType) {
        return function.getDeclaringClass().equals(expectingType);
    }

}
