package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;

public class DataRetrieverChainDefinitionWithSettings<DataSourceType, DataType, Settings extends com.sap.sse.common.settings.Settings> extends
        AbstractDataRetrieverChainDefinition<DataSourceType, DataType, Settings> {

    private final Settings settings;

    public DataRetrieverChainDefinitionWithSettings(Class<DataSourceType> dataSourceType,
            Class<DataType> retrievedDataType, String nameMessageKey, Settings settings) {
        super(dataSourceType, retrievedDataType, nameMessageKey);
        this.settings = settings;
    }
    
    public DataRetrieverChainDefinitionWithSettings(
            DataRetrieverChainDefinition<DataSourceType, ?, ?> dataRetrieverChainDefinition,
            Class<DataType> retrievedDataType, String nameMessageKey, Settings settings) {
        super(dataRetrieverChainDefinition, retrievedDataType, nameMessageKey);
        this.settings = settings;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }
    
}
