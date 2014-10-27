package com.sap.sailing.datamining;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sse.datamining.ClassesWithFunctionsRegistrationService;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.DataRetrieverChainDefinitionRegistrationService;

public class Activator implements BundleActivator {
    
    private static final Logger LOGGER = Logger.getLogger(Activator.class.getSimpleName());

    private SailingDataRetrieverChainDefinitions dataRetrieverChainDefinitions;
    
    private ServiceReference<ClassesWithFunctionsRegistrationService> classesWithFunctionsRegistrationServiceReference;
    private ServiceReference<DataRetrieverChainDefinitionRegistrationService> dataRetrieverChainDefinitionRegistrationServiceServiceReference;

    @Override
    public void start(BundleContext context) throws Exception {
        dataRetrieverChainDefinitions = new SailingDataRetrieverChainDefinitions();
        
        classesWithFunctionsRegistrationServiceReference = context.getServiceReference(ClassesWithFunctionsRegistrationService.class);
        if (classesWithFunctionsRegistrationServiceReference != null) {
            context.getService(classesWithFunctionsRegistrationServiceReference)
                    .registerInternalClassesWithMarkedMethods(getInternalClassesWithMarkedMethods());
            context.getService(classesWithFunctionsRegistrationServiceReference)
                    .registerExternalLibraryClasses(getExternalLibraryClasses());
        } else {
            LOGGER.log(Level.WARNING, "Couldn't register the sailing classes with functions. No registration service was available.");
        }
        
        dataRetrieverChainDefinitionRegistrationServiceServiceReference = context.getServiceReference(DataRetrieverChainDefinitionRegistrationService.class);
        if (dataRetrieverChainDefinitionRegistrationServiceServiceReference != null) {
            DataRetrieverChainDefinitionRegistrationService dataRetrieverChainDefinitionRegistrationService = context.getService(dataRetrieverChainDefinitionRegistrationServiceServiceReference);
            for (DataRetrieverChainDefinition<?> dataRetrieverChainDefinition : dataRetrieverChainDefinitions.getDataRetrieverChainDefinitions()) {
                dataRetrieverChainDefinitionRegistrationService.addDataRetrieverChainDefinition(dataRetrieverChainDefinition);
            }
        } else {
            LOGGER.log(Level.WARNING, "Couldn't register the sailing data retriever chain definitions. No registration service was available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (classesWithFunctionsRegistrationServiceReference != null) {
            context.getService(classesWithFunctionsRegistrationServiceReference).unregisterAllFunctionsOf(
                    getInternalClassesWithMarkedMethods());
            context.getService(classesWithFunctionsRegistrationServiceReference).unregisterAllFunctionsOf(
                    getExternalLibraryClasses());
        }
        
        if (dataRetrieverChainDefinitionRegistrationServiceServiceReference != null) {
            for (DataRetrieverChainDefinition<?> dataRetrieverChainDefinition : dataRetrieverChainDefinitions.getDataRetrieverChainDefinitions()) {
                context.getService(dataRetrieverChainDefinitionRegistrationServiceServiceReference).removeDataRetrieverChainDefinition(dataRetrieverChainDefinition);
            }
        }
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
        return Collections.emptySet();
    }

}
