package com.sap.sse.datamining.impl.components;

import java.util.Map;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.settings.Setting;
import com.sap.sse.common.settings.SettingType;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition.EmptySettings;

@SuppressWarnings("rawtypes")
public class SimpleDataRetrieverChainDefinition<DataSourceType, DataType> extends
        AbstractDataRetrieverChainDefinition<DataSourceType, DataType, EmptySettings> {

    public SimpleDataRetrieverChainDefinition(Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType,
            String nameMessageKey) {
        super(dataSourceType, retrievedDataType, nameMessageKey);
    }
    

    public SimpleDataRetrieverChainDefinition(
            DataRetrieverChainDefinition<DataSourceType, ?, ?> leaderboardRetrieverChainDefinition,
            Class<DataType> retrievedDataType, String nameMessageKey) {
        super(leaderboardRetrieverChainDefinition, retrievedDataType, nameMessageKey);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public EmptySettings getSettings() {
        return new EmptySettings();
    }
    
    public class EmptySettings extends SerializableSettings {

        private static final long serialVersionUID = 2731413350062447794L;

        @Override
        public SettingType getType() {
            return null;
        }

        @Override
        public Map<String, Setting> getNonDefaultSettings() {
            return null;
        }

    }

    @Override
    public void setSettings(SerializableSettings settings) {}

}
