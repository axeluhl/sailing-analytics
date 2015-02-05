package com.sap.sse.datamining;

import com.sap.sse.i18n.ServerStringMessages;

public interface DataMiningBundleService {
    
    public ServerStringMessages getStringMessages();

    public Iterable<Class<?>> getInternalClassesWithMarkedMethods();
    public Iterable<Class<?>> getExternalLibraryClasses();
    
    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions();

}
