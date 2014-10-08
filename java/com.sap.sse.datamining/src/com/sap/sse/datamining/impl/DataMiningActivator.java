package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sse.datamining.ClassesWithFunctionsProvider;
import com.sap.sse.datamining.ClassesWithFunctionsService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.functions.RegistryFunctionProvider;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;

public class DataMiningActivator implements BundleActivator, ClassesWithFunctionsProvider {

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

        FunctionRegistry functionRegistry = new SimpleFunctionRegistry();
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        dataMiningServer = new DataMiningServerImpl(stringMessages, functionRegistry, functionProvider, this);
        registerDataMiningServer();
    }

    private void registerDataMiningServer() {
        context.registerService(DataMiningServer.class, dataMiningServer, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
    
    public static DataMiningStringMessages getStringMessages() {
        return stringMessages;
    }
    
    public static BundleContext getContext() {
        return context;
    }

    @Override
    public Set<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClassesWithMarkedMethods = new HashSet<>();
        
        for (ServiceReference<ClassesWithFunctionsService> serviceReference : getAllClassesWithMarkedMethodsServices()) {
            internalClassesWithMarkedMethods.addAll(getInternalClassesWithMarkedMethodsFromService(serviceReference));
        }
        return internalClassesWithMarkedMethods;
    }

    @Override
    public Set<Class<?>> getExternalLibraryClasses() {
        Set<Class<?>> externalLibraryClasses = new HashSet<>();
        
        for (ServiceReference<ClassesWithFunctionsService> serviceReference : getAllClassesWithMarkedMethodsServices()) {
            externalLibraryClasses.addAll(getExternalLibraryClassesFromService(serviceReference));
        }
        return externalLibraryClasses;
    }

    private Collection<ServiceReference<ClassesWithFunctionsService>> getAllClassesWithMarkedMethodsServices() {
        try {
            ServiceReference<?>[] serviceReferences = context.getServiceReferences(ClassesWithFunctionsService.class.getName(), null);
            if (serviceReferences != null) {
                return castServiceReferences(serviceReferences);
            }
        } catch (InvalidSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Error getting the service references. Data mining won't work.", e);
        }
        LOGGER.log(Level.SEVERE, "There were no " + ClassesWithFunctionsService.class.getName() + "-Services registered. Data mining won't work.");
        return new ArrayList<>();
    }

    private Collection<ServiceReference<ClassesWithFunctionsService>> castServiceReferences(ServiceReference<?>[] serviceReferences) {
        Collection<ServiceReference<ClassesWithFunctionsService>> specificServiceReferences = new ArrayList<>();
        for (ServiceReference<?> serviceReference : serviceReferences) {
            try {
                @SuppressWarnings("unchecked") // This is necessary, because you can't use instanceof with specific generics
                ServiceReference<ClassesWithFunctionsService> specificServiceReference = (ServiceReference<ClassesWithFunctionsService>) serviceReference;
                specificServiceReferences.add(specificServiceReference);
            } catch (ClassCastException exception) {
                String serviceName = context.getService(serviceReference).getClass().getName();
                LOGGER.log(Level.WARNING, "Couldn't register the service '" + serviceName + "' to the function registry."
                        + " Data mining functionalities will be restricted.", exception);
            }
        }
        return specificServiceReferences;
    }

    private Collection<Class<?>> getInternalClassesWithMarkedMethodsFromService(ServiceReference<ClassesWithFunctionsService> serviceReference) {
        Collection<Class<?>> internalClassesWithMarkedMethods = context.getService(serviceReference).getInternalClassesWithMarkedMethods();
        return internalClassesWithMarkedMethods != null ? internalClassesWithMarkedMethods : new ArrayList<Class<?>>();
    }
    
    private Collection<Class<?>> getExternalLibraryClassesFromService(ServiceReference<ClassesWithFunctionsService> serviceReference) {
        Collection<Class<?>> externalLibraryClasses = context.getService(serviceReference).getExternalLibraryClasses();
        return externalLibraryClasses != null ? externalLibraryClasses : new ArrayList<Class<?>>();
    }

}
