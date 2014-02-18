package com.sap.sailing.datamining.impl;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.datamining.DataMiningService;
import com.sap.sailing.datamining.impl.function.DataMiningClassesWithFunctionsService;
import com.sap.sailing.datamining.impl.function.PartitionParallelExternalFunctionRetriever;
import com.sap.sailing.datamining.impl.function.PartitioningParallelMarkedFunctionRetriever;
import com.sap.sailing.datamining.impl.function.RegistryFunctionsProvider;
import com.sap.sailing.datamining.impl.function.SimpleFunctionRegistry;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.functions.ParallelFunctionRetriever;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    private static BundleContext context;
    
    private static DataMiningService dataMiningService;

    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;

        registerDataMiningClassesWithMarkedMethodsService();

        FunctionRegistry functionRegistry = createAndBuildFunctionRegistry();
        FunctionProvider functionProvider = new RegistryFunctionsProvider(functionRegistry);
        
        dataMiningService = new DataMiningServiceImpl(functionRegistry, functionProvider);
        registerDataMiningService();
    }

    private void registerDataMiningService() {
        context.registerService(DataMiningService.class, dataMiningService, null);
    }

    private void registerDataMiningClassesWithMarkedMethodsService() {
        context.registerService(ClassesWithFunctionsService.class, new DataMiningClassesWithFunctionsService(), null);
    }

    private FunctionRegistry createAndBuildFunctionRegistry() {
        FunctionRegistry functionRegistry = new SimpleFunctionRegistry();
        for (ServiceReference<?> serviceReference : getAllClassesWithMarkedMethodsServices()) {
            registerServiceTo(serviceReference, functionRegistry);
        }
        return functionRegistry;
    }

    private ServiceReference<?>[] getAllClassesWithMarkedMethodsServices() {
        try {
            return context.getServiceReferences(ClassesWithFunctionsService.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Error getting the service references. Data mining won't work.", e);
        }
        return new ServiceReference<?>[0];
    }

    @SuppressWarnings("unchecked")
    private void registerServiceTo(ServiceReference<?> serviceReference, FunctionRegistry functionRegistry) {
        try {
            ServiceReference<ClassesWithFunctionsService> specificServiceReference = (ServiceReference<ClassesWithFunctionsService>) serviceReference;
            registerSpecificServiceTo(context.getService(specificServiceReference), functionRegistry);
        } catch (ClassCastException exception) {
            String serviceName = context.getService(serviceReference).getClass().getName();
            LOGGER.log(Level.WARNING, "Couldn't register the service '" + serviceName + "' to the function registry."
                    + " Data mining functionalities will be restricted.", exception);
            return;
        }
    }

    private void registerSpecificServiceTo(ClassesWithFunctionsService service, FunctionRegistry functionRegistry) {
        if (service.hasInternalClassesWithMarkedMethods()) {
            registerInternalMarkedFunctionsTo(service, functionRegistry);
        }
        if (service.hasExternalLibraryClasses()) {
            registerExternalLibraryFunctionsTo(service, functionRegistry);
        }
    }

    public void registerInternalMarkedFunctionsTo(ClassesWithFunctionsService service, FunctionRegistry functionRegistry) {
        Collection<Class<?>> internalClasses = service.getInternalClassesWithMarkedMethods();
        ParallelFunctionRetriever internalMarkedFunctionsRetriever = new PartitioningParallelMarkedFunctionRetriever(
                internalClasses, getExecutor());
        functionRegistry.registerFunctionsRetrievedBy(internalMarkedFunctionsRetriever);
    }

    public void registerExternalLibraryFunctionsTo(ClassesWithFunctionsService service,
            FunctionRegistry functionRegistry) {
        Collection<Class<?>> externalClasses = service.getExternalLibraryClasses();
        ParallelFunctionRetriever externalLibraryFunctionsRetriever = new PartitionParallelExternalFunctionRetriever(
                externalClasses, getExecutor());
        functionRegistry.registerFunctionsRetrievedBy(externalLibraryFunctionsRetriever);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceReference<?> serviceReference : getAllClassesWithMarkedMethodsServices()) {
            context.ungetService(serviceReference);
        }
    }

    public static ThreadPoolExecutor getExecutor() {
        return DataMiningServiceImpl.getExecutor();
    }

    public static FunctionProvider getFunctionProvider() {
        return dataMiningService.getFunctionProvider();
    }

}
