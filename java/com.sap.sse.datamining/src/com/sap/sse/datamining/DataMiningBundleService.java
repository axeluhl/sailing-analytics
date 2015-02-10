package com.sap.sse.datamining;

import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface DataMiningBundleService {
    
    public ResourceBundleStringMessages getStringMessages();

    public Iterable<Class<?>> getInternalClassesWithMarkedMethods();
    public Iterable<Class<?>> getExternalLibraryClasses();
    
    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions();

}
