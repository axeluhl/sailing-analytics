package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.DataMiningBundleService;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.DataSourceProvider;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.AbstractDataMiningActivator;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;

public class Activator extends AbstractDataMiningActivator implements DataMiningBundleService {
    
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/Sailing_StringMessages";
    private static final SailingClusterGroups clusterGroups = new SailingClusterGroups();
    
    private static Activator INSTANCE;
    
    private BundleContext context = null;

    private final ResourceBundleStringMessages sailingServerStringMessages;
    private final SailingDataRetrievalChainDefinitions dataRetrieverChainDefinitions;
    private Collection<DataSourceProvider<?>> dataSourceProviders;
    private boolean dataSourceProvidersHaveBeenInitialized;
    
    public Activator() {
        dataRetrieverChainDefinitions = new SailingDataRetrievalChainDefinitions();
        sailingServerStringMessages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME, getClass().getClassLoader());
    }

    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        this.context = context;
        dataSourceProvidersHaveBeenInitialized = false;
        super.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.context = null;
        INSTANCE = null;
        super.stop(context);
    }
    
    @Override
    protected DataMiningBundleService getDataMiningBundleService() {
        return this;
    }
    
    @Override
    public ResourceBundleStringMessages getStringMessages() {
        return sailingServerStringMessages;
    }

    @Override
    public Iterable<Class<?>> getClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(HasTrackedRaceContext.class);
        internalClasses.add(HasRaceResultOfCompetitorContext.class);
        internalClasses.add(HasTrackedLegContext.class);
        internalClasses.add(HasTrackedLegOfCompetitorContext.class);
        internalClasses.add(HasGPSFixContext.class);
        internalClasses.add(HasMarkPassingContext.class);
        return internalClasses;
    }

    @Override
    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions.getDataRetrieverChainDefinitions();
    }
    
    @Override
    public Iterable<DataSourceProvider<?>> getDataSourceProviders() {
        if (!dataSourceProvidersHaveBeenInitialized) {
            initializeDataSourceProviders();
            dataSourceProvidersHaveBeenInitialized = true;
        }
        return dataSourceProviders;
    }
    
    private void initializeDataSourceProviders() {
        dataSourceProviders = new HashSet<>();
        dataSourceProviders.add(new RacingEventServiceProvider(context));
    }

    public static ClusterGroup<Speed> getWindStrengthInBeaufortClusterGroup() {
        return clusterGroups.getWindStrengthInBeaufortCluster();
    }
    
    public static Activator getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        
        return INSTANCE;
    }

}
