package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.datamining.DataMiningBundleService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.impl.functions.FunctionManager;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;

public class DataMiningActivator implements BundleActivator {

    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    
    private static DataMiningActivator INSTANCE;

    private ServiceTracker<DataMiningBundleService, DataMiningBundleService> dataMiningBundleServiceTracker;
    private final Collection<ServiceRegistration<?>> serviceRegistrations;
    
    private final ModifiableDataMiningServer dataMiningServer;
    
    public DataMiningActivator() {
        ExecutorService executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        FunctionManager functionManager = new FunctionManager();
        DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry = new SimpleDataRetrieverChainDefinitionRegistry();
        dataMiningServer = new DataMiningServerImpl(executor, functionManager, functionManager, dataRetrieverChainDefinitionRegistry);
        dataMiningServer.addStringMessages(new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME, DataMiningActivator.class.getClassLoader()));
        
        serviceRegistrations = new HashSet<>();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        
        dataMiningBundleServiceTracker = new ServiceTracker<>(context, DataMiningBundleService.class, new ServiceTrackerCustomizer<DataMiningBundleService, DataMiningBundleService>() {
            @Override
            public DataMiningBundleService addingService(ServiceReference<DataMiningBundleService> reference) {
                DataMiningBundleService dataMiningBundleService = context.getService(reference);
                registerDataMiningBundle(dataMiningBundleService);
                return dataMiningBundleService;
            }
            @Override
            public void modifiedService(ServiceReference<DataMiningBundleService> reference,
                    DataMiningBundleService service) { }
            @Override
            public void removedService(ServiceReference<DataMiningBundleService> reference,
                    DataMiningBundleService dataMiningBundleService) {
                unregisterDataMiningBundle(dataMiningBundleService);
            }
        });
        dataMiningBundleServiceTracker.open();

        serviceRegistrations.add(context.registerService(DataMiningServer.class, dataMiningServer, null));
    }

    private void registerDataMiningBundle(DataMiningBundleService dataMiningBundleService) {
        dataMiningServer.addStringMessages(dataMiningBundleService.getStringMessages());
        
        dataMiningServer.registerAllClasses(dataMiningBundleService.getClassesWithMarkedMethods());
        
        for (DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition : dataMiningBundleService.getDataRetrieverChainDefinitions()) {
            dataMiningServer.registerDataRetrieverChainDefinition(dataRetrieverChainDefinition);
        }
    }

    private void unregisterDataMiningBundle(DataMiningBundleService dataMiningBundleService) {
        dataMiningServer.removeStringMessages(dataMiningBundleService.getStringMessages());
        
        dataMiningServer.unregisterAllFunctionsOf(dataMiningBundleService.getClassesWithMarkedMethods());
        
        for (DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition : dataMiningBundleService.getDataRetrieverChainDefinitions()) {
            dataMiningServer.unregisterDataRetrieverChainDefinition(dataRetrieverChainDefinition);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        dataMiningBundleServiceTracker.close();
        
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
