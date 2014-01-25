package com.sap.sailing.datamining.function.impl;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.function.Function;

public class DeclaringTypeOrParameterTypeCriteria implements ConcurrentFilterCriteria<Function> {

    private Collection<Class<?>> expectingTypes;

    public DeclaringTypeOrParameterTypeCriteria(Class<?> expectingType) {
        this.expectingTypes = getSuperClassesAndInterfacesOf(expectingType);
        this.expectingTypes.add(expectingType);
    }

    private Collection<Class<?>> getSuperClassesAndInterfacesOf(Class<?> expectingType) {
        Collection<Class<?>> superClassesAndInterfaces = new HashSet<>();
        
        for (Class<?> interfaceOfExpectingType : expectingType.getInterfaces()) {
            superClassesAndInterfaces.add(interfaceOfExpectingType);
        }
        
        Class<?> currentType = expectingType;
        while (currentType.getSuperclass() != null && !currentType.getSuperclass().equals(Object.class)) {
            currentType = currentType.getSuperclass();
            superClassesAndInterfaces.add(currentType);
        }
        return superClassesAndInterfaces;
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
