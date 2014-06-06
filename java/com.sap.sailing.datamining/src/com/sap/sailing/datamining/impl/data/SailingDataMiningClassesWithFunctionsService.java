package com.sap.sailing.datamining.impl.data;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;

public class SailingDataMiningClassesWithFunctionsService implements ClassesWithFunctionsService {

    @Override
    public Set<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        /*
         * This also contains the marked methods of
         * HasTrackedRaceContext,
         * HasTrackedLegContext and
         * HasTrackedLegOfCompetitorContext,
         * because these are the super types of HasGPSFixContext.
         */
        internalClasses.add(HasGPSFixContext.class);
        return internalClasses;
    }

    @Override
    public Set<Class<?>> getExternalLibraryClasses() {
        return new HashSet<>();
    }

}
