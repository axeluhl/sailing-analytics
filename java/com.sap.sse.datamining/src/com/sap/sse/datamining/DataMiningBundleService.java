package com.sap.sse.datamining;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;

public interface DataMiningBundleService {
    
    public DataMiningStringMessages getStringMessages();

    public Iterable<Class<?>> getInternalClassesWithMarkedMethods();
    public Iterable<Class<?>> getExternalLibraryClasses();
    
    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions();

}
