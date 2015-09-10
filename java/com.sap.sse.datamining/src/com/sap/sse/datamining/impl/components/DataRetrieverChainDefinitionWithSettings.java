package com.sap.sse.datamining.impl.components;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;

public class DataRetrieverChainDefinitionWithSettings<DataSourceType, DataType, Settings extends com.sap.sse.common.settings.SerializableSettings> extends
        AbstractDataRetrieverChainDefinition<DataSourceType, DataType, Settings> {

    private final SerializableSettings settings;

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
    public SerializableSettings getSettings() {
        return settings;
    }
    
}
