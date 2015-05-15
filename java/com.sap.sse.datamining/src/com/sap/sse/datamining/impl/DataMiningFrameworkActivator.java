package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.datamining.DataMiningBundleService;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.DataSourceProvider;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.management.AggregationProcessorDefinitionRegistry;
import com.sap.sse.datamining.components.management.DataRetrieverChainDefinitionRegistry;
import com.sap.sse.datamining.impl.components.management.AggregationProcessorDefinitionManager;
import com.sap.sse.datamining.impl.components.management.SimpleDataRetrieverChainDefinitionManager;
import com.sap.sse.datamining.impl.functions.FunctionManager;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;

public class DataMiningFrameworkActivator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(DataMiningFrameworkActivator.class.getName());
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";
    private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
    
    private static DataMiningFrameworkActivator INSTANCE;

    private ServiceTracker<DataMiningBundleService, DataMiningBundleService> dataMiningBundleServiceTracker;
    private final Collection<ServiceRegistration<?>> serviceRegistrations;
    
    private final ModifiableDataMiningServer dataMiningServer;
    
    public DataMiningFrameworkActivator() {
        ExecutorService executor = new DataMiningExecutorService(THREAD_POOL_SIZE);

        FunctionManager functionManager = new FunctionManager();
        DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionManager = new SimpleDataRetrieverChainDefinitionManager();
        AggregationProcessorDefinitionRegistry aggregationProcessorDefinitionManager = new AggregationProcessorDefinitionManager();
        dataMiningServer = new DataMiningServerImpl(executor, functionManager, dataRetrieverChainDefinitionManager, aggregationProcessorDefinitionManager);
        dataMiningServer.addStringMessages(new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME, DataMiningFrameworkActivator.class.getClassLoader()));
        
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
        logger.info("Registering data mining bundle "+dataMiningBundleService);
        dataMiningServer.addStringMessages(dataMiningBundleService.getStringMessages());
        dataMiningServer.registerAllClasses(dataMiningBundleService.getClassesWithMarkedMethods());
        for (DataSourceProvider<?> dataSourceProvider : dataMiningBundleService.getDataSourceProviders()) {
            dataMiningServer.setDataSourceProvider(dataSourceProvider);
        }
        for (DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition : dataMiningBundleService.getDataRetrieverChainDefinitions()) {
            dataMiningServer.registerDataRetrieverChainDefinition(dataRetrieverChainDefinition);
        }
        for (AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition : dataMiningBundleService.getAggregationProcessorDefinitions()) {
            dataMiningServer.registerAggregationProcessor(aggregationProcessorDefinition);
        }
    }

    private void unregisterDataMiningBundle(DataMiningBundleService dataMiningBundleService) {
        logger.info("Unregistering data mining bundle "+dataMiningBundleService);
        dataMiningServer.removeStringMessages(dataMiningBundleService.getStringMessages());
        dataMiningServer.unregisterAllFunctionsOf(dataMiningBundleService.getClassesWithMarkedMethods());
        for (DataSourceProvider<?> dataSourceProvider : dataMiningBundleService.getDataSourceProviders()) {
            dataMiningServer.removeDataSourceProvider(dataSourceProvider);
        }
        for (DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition : dataMiningBundleService.getDataRetrieverChainDefinitions()) {
            dataMiningServer.unregisterDataRetrieverChainDefinition(dataRetrieverChainDefinition);
        }
        for (AggregationProcessorDefinition<?, ?> aggregationProcessorDefinition : dataMiningBundleService.getAggregationProcessorDefinitions()) {
            dataMiningServer.unregisterAggregationProcessor(aggregationProcessorDefinition);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        dataMiningBundleServiceTracker.close();
        
        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
            context.ungetService(serviceRegistration.getReference());
        }
    }
    
    public static DataMiningFrameworkActivator getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new DataMiningFrameworkActivator(); // probably non-OSGi case, as in test execution
        }
        return INSTANCE;
    }

}
