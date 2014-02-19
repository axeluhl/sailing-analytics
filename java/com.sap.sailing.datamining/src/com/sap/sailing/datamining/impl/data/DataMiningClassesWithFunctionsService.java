package com.sap.sailing.datamining.impl.data;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;

public class DataMiningClassesWithFunctionsService implements ClassesWithFunctionsService {
    
    @Override
    public boolean hasInternalClassesWithMarkedMethods() {
        return true;
    }

    @Override
    public Collection<Class<?>> getInternalClassesWithMarkedMethods() {
        Collection<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(GPSFixWithContext.class);
        internalClasses.add(TrackedLegOfCompetitorWithContext.class);
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
