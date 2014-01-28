package com.sap.sailing.datamining.function.impl;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.function.ClassesWithFunctionsService;

public class DataMiningClassesWithFunctionsService implements ClassesWithFunctionsService {
    
    @Override
    public boolean hasInternalClassesWithMarkedMethods() {
        return true;
    }

    @Override
    public Collection<Class<?>> getInternalClassesWithMarkedMethods() {
        Collection<Class<?>> internalClasses = new HashSet<>();
        
        return internalClasses;
    }
    
    @Override
    public boolean hasExternalLibraryClasses() {
        return false;
    }

    @Override
    public Collection<Class<?>> getExternalLibraryClasses() {
        return new HashSet<>();
    }

}
