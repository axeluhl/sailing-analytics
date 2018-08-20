package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.components.DataRetrieverChainBuilder;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class SimpleDataRetrieverChainDefinition<DataSourceType, DataType> implements
        DataRetrieverChainDefinition<DataSourceType, DataType> {
    
    private final Class<DataSourceType> dataSourceType;
    private final Class<DataType> retrievedDataType;
    private final List<DataRetrieverLevel<?, ?>> dataRetrieverTypesWithInformation;

    private final String nameMessageKey;
    protected boolean isComplete;

    public SimpleDataRetrieverChainDefinition(Class<DataSourceType> dataSourceType, Class<DataType> retrievedDataType, String nameMessageKey) {
        this.dataSourceType = dataSourceType;
        this.retrievedDataType = retrievedDataType;
        dataRetrieverTypesWithInformation = new ArrayList<>();

        this.nameMessageKey = nameMessageKey;
        isComplete = false;
    }

    public SimpleDataRetrieverChainDefinition(DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, Class<DataType> retrievedDataType, String nameMessageKey) {
        this(dataRetrieverChainDefinition.getDataSourceType(), retrievedDataType, nameMessageKey);
        dataRetrieverTypesWithInformation.addAll(dataRetrieverChainDefinition.getDataRetrieverLevels());
    }
    
    @Override
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages) {
        return stringMessages.get(locale, nameMessageKey);
    }

    @Override
    public Class<DataSourceType> getDataSourceType() {
        return dataSourceType;
    }
    
    @Override
    public Class<DataType> getRetrievedDataType() {
        return retrievedDataType;
    }
    
    @Override
    public <ResultType> void startWith(Class<? extends Processor<DataSourceType, ResultType>> retrieverType,
            Class<ResultType> retrievedDataType, String retrievedDataTypeMessageKey) {
        startWith(retrieverType, retrievedDataType, /* settings type */ null, /* default settings */ null, retrievedDataTypeMessageKey);
    }

    @Override
    public <ResultType, SettingsType extends SerializableSettings> void startWith(Class<? extends Processor<DataSourceType, ResultType>> retrieverType,
                                       Class<ResultType> retrievedDataType, Class<SettingsType> settingsType, SettingsType defaultSettings,
                                       String retrievedDataTypeMessageKey) {
        if (isInitialized()) {
            throw new IllegalStateException("This retriever chain definition already has been started with '"
                                                    + dataRetrieverTypesWithInformation.get(0).getRetrieverType().getSimpleName() + "'");
        }
        checkThatRetrieverHasUsableConstructor(retrieverType, settingsType);
        if (settingsType != null && defaultSettings == null) {
            throw new NullPointerException("No default settings have been given");
        }
        DataRetrieverLevel<?, ?> retrieverTypeWithInformation = new DataRetrieverLevel<>(
                dataRetrieverTypesWithInformation.size(), retrieverType, retrievedDataType, settingsType, retrievedDataTypeMessageKey, defaultSettings);
        dataRetrieverTypesWithInformation.add(retrieverTypeWithInformation);
    }

    private boolean isInitialized() {
        return !dataRetrieverTypesWithInformation.isEmpty();
    }
    
    @Override
    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void addAfter(
            Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
            Class<? extends Processor<NextInputType, NextResultType>> nextRetrieverType,
            Class<NextResultType> retrievedDataType, String retrievedDataTypeMessageKey) {
        addAfter(lastAddedRetrieverType, nextRetrieverType, retrievedDataType,
                /* settingsType */ null, /* default settings */ null, retrievedDataTypeMessageKey);
    }

    @Override
    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType, SettingsType extends SerializableSettings> void addAfter(
            Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
            Class<? extends Processor<NextInputType, NextResultType>> nextRetrieverType,
            Class<NextResultType> retrievedDataType, Class<SettingsType> settingsType,
            SettingsType defaultSettings, String retrievedDataTypeMessageKey) {
        if (!isInitialized()) {
            throw new IllegalStateException("This retriever chain definition hasn't been started yet");
        }
        if (isComplete) {
            throw new IllegalStateException("This retriever chain definition is already complete");
        }
        DataRetrieverLevel<?, ?> dataRetrieverTypeWithInformation = dataRetrieverTypesWithInformation.get(dataRetrieverTypesWithInformation.size() - 1);
        @SuppressWarnings("unchecked")
        Class<Processor<?, ?>> lastRetrieverInList = (Class<Processor<?, ?>>)(Class<?>) dataRetrieverTypeWithInformation.getRetrieverType();
        if (!lastRetrieverInList.equals(lastAddedRetrieverType)) {
            throw new IllegalArgumentException("The given previousRetrieverType '" + lastAddedRetrieverType.getSimpleName()
                                               + "' doesn't match the last retriever type in the chain '" 
                                               + lastRetrieverInList.getSimpleName() + "'");
        }
        checkThatRetrieverHasUsableConstructor(nextRetrieverType, settingsType);
        if (settingsType != null && defaultSettings == null) {
            throw new NullPointerException("No default settings have been given");
        }

        DataRetrieverLevel<?, ?> retrieverTypeWithInformation = new DataRetrieverLevel<>(
                dataRetrieverTypesWithInformation.size(), nextRetrieverType, retrievedDataType,
                settingsType, retrievedDataTypeMessageKey, defaultSettings);
        dataRetrieverTypesWithInformation.add(retrieverTypeWithInformation);
    }

    private <InputType, ResultType, SettingsType> void checkThatRetrieverHasUsableConstructor(
            Class<? extends Processor<InputType, ResultType>> retrieverType, Class<SettingsType> settingsType) {
        try {
            if (settingsType == null) {
                retrieverType.getConstructor(ExecutorService.class, Collection.class, int.class);
            } else {
                retrieverType.getConstructor(ExecutorService.class, Collection.class, settingsType, int.class);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Couldn't get an usable constructor from the given nextRetrieverType '"
                    + retrieverType.getSimpleName() + "'", e);
        }
    }
    
    @Override
    public <NextInputType, PreviousInputType, PreviousResultType extends NextInputType> void endWith(
            Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
            Class<? extends Processor<NextInputType, DataType>> lastRetrieverType, Class<DataType> retrievedDataType,
            String retrievedDataTypeMessageKey) {
        endWith(lastAddedRetrieverType, lastRetrieverType, retrievedDataType,
                /* settings type */ null, /* default settings */ null, retrievedDataTypeMessageKey);
    }
    
    @Override
    public <NextInputType, PreviousInputType, PreviousResultType extends NextInputType, SettingsType extends SerializableSettings> void endWith(
            Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
            Class<? extends Processor<NextInputType, DataType>> lastRetrieverType, Class<DataType> retrievedDataType,
            Class<SettingsType> settingsType, SettingsType defaultSettings, String retrievedDataTypeMessageKey) {
        addAfter(lastAddedRetrieverType, lastRetrieverType, retrievedDataType, settingsType, defaultSettings, retrievedDataTypeMessageKey);
        isComplete = true;
    }
    
    @Override
    public List<? extends DataRetrieverLevel<?, ?>> getDataRetrieverLevels() {
        return dataRetrieverTypesWithInformation;
    }
    
    @Override
    public DataRetrieverLevel<?, ?> getDataRetrieverLevel(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= getDataRetrieverLevels().size()) {
            return null;
        }
        return getDataRetrieverLevels().get(levelIndex);
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor) {
        if (!isComplete) {
            throw new IllegalStateException("This retriever chain definition hasn't been completed yet");
        }
        
        return new SimpleDataRetrieverChainBuilder<>(executor, dataRetrieverTypesWithInformation);
    }
    
    @Override
    public String toString() {
        return getDataSourceType().getSimpleName() + " -> " + getRetrievedDataType().getSimpleName() +
               "[messageKey: " + nameMessageKey + ", isComplete: " + isComplete + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((dataRetrieverTypesWithInformation == null) ? 0 : dataRetrieverTypesWithInformation.hashCode());
        result = prime * result + ((dataSourceType == null) ? 0 : dataSourceType.hashCode());
        result = prime * result + ((retrievedDataType == null) ? 0 : retrievedDataType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleDataRetrieverChainDefinition<?, ?> other = (SimpleDataRetrieverChainDefinition<?, ?>) obj;
        if (dataRetrieverTypesWithInformation == null) {
            if (other.dataRetrieverTypesWithInformation != null)
                return false;
        } else if (!dataRetrieverTypesWithInformation.equals(other.dataRetrieverTypesWithInformation))
            return false;
        if (dataSourceType == null) {
            if (other.dataSourceType != null)
                return false;
        } else if (!dataSourceType.equals(other.dataSourceType))
            return false;
        if (retrievedDataType == null) {
            if (other.retrievedDataType != null)
                return false;
        } else if (!retrievedDataType.equals(other.retrievedDataType))
            return false;
        return true;
    }

}
