package com.sap.sailing.datamining;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sse.datamining.DataMiningBundleService;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;

public class Activator implements BundleActivator, DataMiningBundleService {
    
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/Sailing_StringMessages";
    
    private static Activator INSTANCE;

    private final ResourceBundleStringMessages sailingServerStringMessages;
    private final SailingDataRetrieverChainDefinitions dataRetrieverChainDefinitions;
    private final SailingClusterGroups clusterGroups;
    
    private ServiceReference<DataMiningBundleService> dataMiningBundleServiceReference;
    
    public Activator() {
        dataRetrieverChainDefinitions = new SailingDataRetrieverChainDefinitions();
        sailingServerStringMessages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME, getClass().getClassLoader());
        clusterGroups = new SailingClusterGroups();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        INSTANCE = this;
        
        dataMiningBundleServiceReference = context.registerService(DataMiningBundleService.class, this, null).getReference();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.ungetService(dataMiningBundleServiceReference);
    }
    
    @Override
    public ResourceBundleStringMessages getStringMessages() {
        return sailingServerStringMessages;
    }

    @Override
    public Iterable<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(HasTrackedRaceContext.class);
        internalClasses.add(HasTrackedLegContext.class);
        internalClasses.add(HasTrackedLegOfCompetitorContext.class);
        internalClasses.add(HasGPSFixContext.class);
        internalClasses.add(HasMarkPassingContext.class);
        return internalClasses;
    }

    @Override
    public Iterable<Class<?>> getExternalLibraryClasses() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions.getDataRetrieverChainDefinitions();
    }
    
    public SailingClusterGroups getClusterGroups() {
        return clusterGroups;
    }
    
    public static Activator getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new Activator(); // probably non-OSGi case, as in test execution
        }
        
        return INSTANCE;
    }

}
