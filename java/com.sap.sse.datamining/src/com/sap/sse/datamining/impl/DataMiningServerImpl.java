package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.functions.ClassesWithFunctionsService;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public class DataMiningServerImpl implements DataMiningServer {
    
    private static final Logger LOGGER = Logger.getLogger(DataMiningServerImpl.class.getSimpleName());

    private final BundleContext context;
    private final Set<ServiceReference<ClassesWithFunctionsService>> registeredServices;
    
    private final FunctionRegistry functionRegistry;
    private final FunctionProvider functionProvider;

    public DataMiningServerImpl(FunctionRegistry functionRegistry, FunctionProvider functionProvider) {
        context = DataMiningActivator.getContext();
        registeredServices = new HashSet<>();
        
        this.functionRegistry = functionRegistry;
        this.functionProvider = functionProvider;
        
        updateRegistryIfNecessary();
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        updateRegistryIfNecessary();
        return functionRegistry;
    }

    @Override
    public FunctionProvider getFunctionProvider() {
        updateRegistryIfNecessary();
        return functionProvider;
    }

    private void updateRegistryIfNecessary() {
        Collection<ServiceReference<ClassesWithFunctionsService>> availableServices = getAllClassesWithMarkedMethodsServices();
        Collection<ServiceReference<ClassesWithFunctionsService>> servicesToRegister = calculateServicesToRegister(availableServices);
        Collection<ServiceReference<ClassesWithFunctionsService>> servicesToUnregister = calculateServicesToUnregister(availableServices);
        
        registerServices(servicesToRegister);
        unregisterServices(servicesToUnregister);
    }

    private Collection<ServiceReference<ClassesWithFunctionsService>> calculateServicesToRegister(
            Collection<ServiceReference<ClassesWithFunctionsService>> availableServices) {
        Collection<ServiceReference<ClassesWithFunctionsService>> servicesToRegister = new ArrayList<>();
        
        for (ServiceReference<ClassesWithFunctionsService> availableService : availableServices) {
            if (!registeredServices.contains(availableService)) {
                servicesToRegister.add(availableService);
            }
        }
        
        return servicesToRegister;
    }

    private Collection<ServiceReference<ClassesWithFunctionsService>> calculateServicesToUnregister(
            Collection<ServiceReference<ClassesWithFunctionsService>> availableServices) {
        Collection<ServiceReference<ClassesWithFunctionsService>> servicesToUnregister = new ArrayList<>();
        for (ServiceReference<ClassesWithFunctionsService> registeredService : registeredServices) {
            if (!availableServices.contains(registeredService)) {
                servicesToUnregister.add(registeredService);
            }
        }
        return servicesToUnregister;
    }

    private void registerServices(Collection<ServiceReference<ClassesWithFunctionsService>> servicesToRegister) {
        Set<Class<?>> internalClassesWithMarkedMethods = new HashSet<>();
        Set<Class<?>> externalLibraryClasses = new HashSet<>();
        for (ServiceReference<ClassesWithFunctionsService> serviceReference : servicesToRegister) {
            internalClassesWithMarkedMethods.addAll(getInternalClassesWithMarkedMethodsFromService(serviceReference));
            externalLibraryClasses.addAll(getExternalLibraryClassesFromService(serviceReference));
        }
        
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesWithMarkedMethods);
        functionRegistry.registerAllWithExternalFunctionPolicy(externalLibraryClasses);
    }

    private void unregisterServices(Collection<ServiceReference<ClassesWithFunctionsService>> servicesToUnregister) {
        Set<Class<?>> classesToUnregister = new HashSet<>();
        for (ServiceReference<ClassesWithFunctionsService> serviceReference : servicesToUnregister) {
            classesToUnregister.addAll(getInternalClassesWithMarkedMethodsFromService(serviceReference));
            classesToUnregister.addAll(getExternalLibraryClassesFromService(serviceReference));
        }
        
        functionRegistry.unregisterAllFunctionsOf(classesToUnregister);
    }
    
    private Collection<Class<?>> getInternalClassesWithMarkedMethodsFromService(ServiceReference<ClassesWithFunctionsService> serviceReference) {
        Collection<Class<?>> internalClassesWithMarkedMethods = context.getService(serviceReference).getInternalClassesWithMarkedMethods();
        return internalClassesWithMarkedMethods != null ? internalClassesWithMarkedMethods : new ArrayList<Class<?>>();
    }
    
    private Collection<Class<?>> getExternalLibraryClassesFromService(ServiceReference<ClassesWithFunctionsService> serviceReference) {
        Collection<Class<?>> externalLibraryClasses = context.getService(serviceReference).getExternalLibraryClasses();
        return externalLibraryClasses != null ? externalLibraryClasses : new ArrayList<Class<?>>();
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
    
}
