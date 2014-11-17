package com.sap.sailing.datamining;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.i18n.DataMiningStringMessagesImpl;

public class Activator implements BundleActivator {
    
    private static final Logger LOGGER = Logger.getLogger(Activator.class.getSimpleName());
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

    private DataMiningStringMessages sailingDataMiningStringMessages;
    private SailingDataRetrieverChainDefinitions dataRetrieverChainDefinitions;
    
    private ServiceReference<ModifiableDataMiningServer> dataMiningServerServiceReference;

    @Override
    public void start(BundleContext context) throws Exception {
        dataRetrieverChainDefinitions = new SailingDataRetrieverChainDefinitions();
        sailingDataMiningStringMessages = new DataMiningStringMessagesImpl(STRING_MESSAGES_BASE_NAME);
        
        dataMiningServerServiceReference = context.getServiceReference(ModifiableDataMiningServer.class);
        if (dataMiningServerServiceReference != null) {
            ModifiableDataMiningServer dataMiningServer = context.getService(dataMiningServerServiceReference);
            
            dataMiningServer.addStringMessages(sailingDataMiningStringMessages);
            
            dataMiningServer.registerAllWithInternalFunctionPolicy(getInternalClassesWithMarkedMethods());
            dataMiningServer.registerAllWithExternalFunctionPolicy(getExternalLibraryClasses());

            for (DataRetrieverChainDefinition<?> dataRetrieverChainDefinition : dataRetrieverChainDefinitions.getDataRetrieverChainDefinitions()) {
                dataMiningServer.registerDataRetrieverChainDefinition(dataRetrieverChainDefinition);
            }
        } else {
            LOGGER.log(Level.WARNING,
                    "Couldn't register the sailing classes with functions and data retriever chain definitions."
                    + " No data mining server was available.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (dataMiningServerServiceReference != null) {
            ModifiableDataMiningServer dataMiningServer = context.getService(dataMiningServerServiceReference);

            dataMiningServer.removeStringMessages(sailingDataMiningStringMessages);
            
            dataMiningServer.unregisterAllFunctionsOf(getInternalClassesWithMarkedMethods());
            dataMiningServer.unregisterAllFunctionsOf(getExternalLibraryClasses());

            for (DataRetrieverChainDefinition<?> dataRetrieverChainDefinition : dataRetrieverChainDefinitions.getDataRetrieverChainDefinitions()) {
                dataMiningServer.unregisterDataRetrieverChainDefinition(dataRetrieverChainDefinition);
            }
        }
    }

    public static Set<Class<?>> getInternalClassesWithMarkedMethods() {
        Set<Class<?>> internalClasses = new HashSet<>();
        internalClasses.add(HasTrackedRaceContext.class);
        internalClasses.add(HasTrackedLegContext.class);
        internalClasses.add(HasTrackedLegOfCompetitorContext.class);
        internalClasses.add(HasGPSFixContext.class);
        return internalClasses;
    }

    public static Set<Class<?>> getExternalLibraryClasses() {
        return Collections.emptySet();
    }

}
