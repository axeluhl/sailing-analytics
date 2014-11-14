package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sse.datamining.ClassesWithFunctionsRegistrationService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.DataRetrieverChainDefinitionRegistrationService;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.functions.RegistryFunctionProvider;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;

public class DataMiningActivator implements BundleActivator, ClassesWithFunctionsRegistrationService, DataRetrieverChainDefinitionRegistrationService {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    
    private static DataMiningActivator INSTANCE;
    
    private final ModifiableDataMiningServer dataMiningServer;
    private final DataMiningStringMessages stringMessages;
    private final ExecutorService executor;
    
    private Collection<ServiceRegistration<?>> serviceRegistrations;
    
    public DataMiningActivator() {
        stringMessages = DataMiningStringMessages.Util.getInstanceFor(STRING_MESSAGES_BASE_NAME);
        executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        FunctionRegistry functionRegistry = new SimpleFunctionRegistry();
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry = new SimpleDataRetrieverChainDefinitionRegistry();
        dataMiningServer = new DataMiningServerImpl(stringMessages, functionRegistry, functionProvider, dataRetrieverChainDefinitionRegistry);
        
        serviceRegistrations = new HashSet<>();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        
        serviceRegistrations.add(context.registerService(DataMiningServer.class, dataMiningServer, null));
        
        serviceRegistrations.add(context.registerService(ClassesWithFunctionsRegistrationService.class, this, null));
        serviceRegistrations.add(context.registerService(DataRetrieverChainDefinitionRegistrationService.class, this, null));
    }

    @Override
    public void registerInternalClassesWithMarkedMethods(Set<Class<?>> classesToScan) {
        dataMiningServer.registerAllWithInternalFunctionPolicy(classesToScan);
    }

    @Override
    public void registerExternalLibraryClasses(Set<Class<?>> externalClassesToScan) {
        dataMiningServer.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }

    @Override
    public void unregisterAllFunctionsOf(Set<Class<?>> classesToUnregister) {
        dataMiningServer.unregisterAllFunctionsOf(classesToUnregister);
    }
    
    @Override
    public void addDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        dataMiningServer.registerDataRetrieverChainDefinition(dataRetrieverChainDefinition);
    }
    
    @Override
    public void removeDataRetrieverChainDefinition(DataRetrieverChainDefinition<?> dataRetrieverChainDefinition) {
        dataMiningServer.unregisterDataRetrieverChainDefinition(dataRetrieverChainDefinition);
        
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
            context.ungetService(serviceRegistration.getReference());
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    
    public DataMiningStringMessages getStringMessages() {
        return stringMessages;
    }
    
    public static DataMiningActivator getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new DataMiningActivator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }

}
