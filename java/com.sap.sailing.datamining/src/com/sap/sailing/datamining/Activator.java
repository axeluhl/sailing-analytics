package com.sap.sailing.datamining;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sse.datamining.ClassesWithFunctionsRegistrationService;

public class Activator implements BundleActivator {
    
    public static final String dataRetrieverGroupName = "Sailing";

    private static BundleContext context;
    private ServiceReference<ClassesWithFunctionsRegistrationService> classesWithFunctionsRegistrationServiceReference;

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
        
        classesWithFunctionsRegistrationServiceReference = Activator.context.getServiceReference(ClassesWithFunctionsRegistrationService.class);
        Activator.context.getService(classesWithFunctionsRegistrationServiceReference).registerInternalClassesWithMarkedMethods(getInternalClassesWithMarkedMethods());
        Activator.context.getService(classesWithFunctionsRegistrationServiceReference).registerExternalLibraryClasses(getExternalLibraryClasses());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        Activator.context.getService(classesWithFunctionsRegistrationServiceReference).unregisterAllFunctionsOf(getInternalClassesWithMarkedMethods());
        Activator.context.getService(classesWithFunctionsRegistrationServiceReference).unregisterAllFunctionsOf(getExternalLibraryClasses());
    }

    public static Set<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(HasTrackedRaceContext.class);
        internalClasses.add(HasTrackedLegContext.class);
        internalClasses.add(HasTrackedLegOfCompetitorContext.class);
        internalClasses.add(HasGPSFixContext.class);
        return internalClasses;
    }

    public static Set<Class<?>> getExternalLibraryClasses() {
        return new HashSet<>();
    }

}
