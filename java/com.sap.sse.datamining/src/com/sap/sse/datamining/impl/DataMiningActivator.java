package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.functions.ParallelFunctionRetriever;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.functions.PartitionParallelExternalFunctionRetriever;
import com.sap.sse.datamining.impl.functions.PartitioningParallelMarkedFunctionRetriever;
import com.sap.sse.datamining.impl.functions.RegistryFunctionsProvider;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;

public class DataMiningActivator implements BundleActivator {

    private static final Logger LOGGER = Logger.getLogger(DataMiningActivator.class.getName());
    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);

    private static BundleContext context;
    
    private static DataMiningServer dataMiningServer;
    private static DataMiningStringMessages stringMessages;
    private static ThreadPoolExecutor executor;

    @Override
    public void start(BundleContext context) throws Exception {
        DataMiningActivator.context = context;
        
        stringMessages = DataMiningStringMessages.Util.getDefaultStringMessages();
        executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                                          new LinkedBlockingQueue<Runnable>());

        FunctionRegistry functionRegistry = createAndBuildFunctionRegistry();
        FunctionProvider functionProvider = new RegistryFunctionsProvider(functionRegistry, getExecutor());
        
        dataMiningServer = new DataMiningServerImpl(functionRegistry, functionProvider);
        registerDataMiningServer();
    }

    private void registerDataMiningServer() {
        context.registerService(DataMiningServer.class, dataMiningServer, null);
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
            ServiceReference<?>[] serviceReferences = context.getServiceReferences(ClassesWithFunctionsService.class.getName(), null);
            if (serviceReferences != null) {
                return serviceReferences;
            }
        } catch (InvalidSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Error getting the service references. Data mining won't work.", e);
        }
        LOGGER.log(Level.SEVERE, "There were no " + ClassesWithFunctionsService.class.getName() + "-Services registered. Data mining won't work.");
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
        return executor;
    }
    
    public static DataMiningStringMessages getStringMessages() {
        return stringMessages;
    }

}
