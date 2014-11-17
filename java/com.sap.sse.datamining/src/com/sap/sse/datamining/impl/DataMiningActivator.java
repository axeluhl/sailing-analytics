package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.functions.RegistryFunctionProvider;
import com.sap.sse.datamining.impl.functions.SimpleFunctionRegistry;

public class DataMiningActivator implements BundleActivator {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    
    private static DataMiningActivator INSTANCE;
    
    private final ModifiableDataMiningServer dataMiningServer;
    
    private Collection<ServiceRegistration<?>> serviceRegistrations;
    
    public DataMiningActivator() {
        ExecutorService executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        FunctionRegistry functionRegistry = new SimpleFunctionRegistry();
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry = new SimpleDataRetrieverChainDefinitionRegistry();
        dataMiningServer = new DataMiningServerImpl(executor, functionRegistry, functionProvider, dataRetrieverChainDefinitionRegistry);
        dataMiningServer.addStringMessages(DataMiningStringMessages.Util.getInstanceFor(STRING_MESSAGES_BASE_NAME));
        
        serviceRegistrations = new HashSet<>();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;

        serviceRegistrations.add(context.registerService(DataMiningServer.class, dataMiningServer, null));
        serviceRegistrations.add(context.registerService(ModifiableDataMiningServer.class, dataMiningServer, null));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
            context.ungetService(serviceRegistration.getReference());
        }
    }
    
    public static DataMiningActivator getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new DataMiningActivator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }

}
