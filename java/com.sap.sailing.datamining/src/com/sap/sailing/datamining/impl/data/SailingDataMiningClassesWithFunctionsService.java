package com.sap.sailing.datamining.impl.data;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;

public class SailingDataMiningClassesWithFunctionsService implements ClassesWithFunctionsService {

    @Override
    public Set<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(HasTrackedRaceContext.class);
        internalClasses.add(HasTrackedLegContext.class);
        internalClasses.add(HasTrackedLegOfCompetitorContext.class);
        internalClasses.add(HasGPSFixContext.class);
        return internalClasses;
    }

    @Override
    public Set<Class<?>> getExternalLibraryClasses() {
        return new HashSet<>();
    }

}
