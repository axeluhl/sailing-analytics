package com.sap.sailing.datamining.impl.data;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;

public class DataMiningClassesWithFunctionsService implements ClassesWithFunctionsService {

    @Override
    public Set<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(GPSFixWithContext.class);
        internalClasses.add(TrackedLegOfCompetitorWithContext.class);
        return internalClasses;
    }

    @Override
    public Set<Class<?>> getExternalLibraryClasses() {
        return new HashSet<>();
    }

}
