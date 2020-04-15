package com.sap.sse.datamining.impl.components;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.components.DataRetrieverChainBuilder;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;

public class SimpleDataRetrieverChainBuilder<DataSourceType> implements DataRetrieverChainBuilder<DataSourceType> {
    
    private final ExecutorService executor;
    private final List<DataRetrieverLevel<?, ?>> retrieverLevels;

    private final Map<Integer, FilterCriterion<?>> filters;
    private final Map<Integer, Collection<Processor<?, ?>>> receivers;
    private final Map<Integer, SerializableSettings> settings;
    private int currentRetrieverLevelIndex;

    /**
     * Creates a data retriever chain builder for the given list of {@link DataRetrieverLevel}.</br>
     * The list has to match the following conditions to build a valid data retriever chain:
     * <ul>
     *  <li>The first data retriever type has the <code>DataSourceType</code> as <code>InputType</code>.</li>
     *  <li>The order of the data retriever types has to be valid.</br>
     *      This means, that the <code>ResultType</code> of a previous data retriever type has to match
     *      the <code>InputType</code> of the next data retriever type.</li>
     * </ul>
     * <b>This builder won't check these conditions!</b></br>
     * It could be possible, that the construction of a data retriever chain, that doesn't match the conditions
     * work, but the resulting data retriever chain will fail, if you try to retrieve data.</br>
     * If you're not forced to create your own instance of this builder, use {@link DataRetrieverChainDefinition#startBuilding()},
     * that always creates a valid builder.
     * @param dataRetrieverTypesWithInformation
     */
    SimpleDataRetrieverChainBuilder(ExecutorService executor, List<DataRetrieverLevel<?, ?>> dataRetrieverTypesWithInformation) {
        this.executor = executor;
        this.retrieverLevels = new ArrayList<>(dataRetrieverTypesWithInformation);
        
        filters = new HashMap<>();
        receivers = new HashMap<>();
        settings = new HashMap<>();
        currentRetrieverLevelIndex = -1;
    }
    
    @Override
    public boolean canStepFurther() {
        return currentRetrieverLevelIndex + 1 < retrieverLevels.size();
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> stepFurther() {
        if (!canStepFurther()) {
            throw new IllegalStateException("The builder can't step any further");
        }
        
        currentRetrieverLevelIndex++;
        return this;
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> setFilter(FilterCriterion<?> filter) {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException("The builder hasn't been initialized");
        }
        
        if (!filter.getElementType().isAssignableFrom(getCurrentRetrievedDataType())) {
            throw new IllegalArgumentException("The given filter (with element type '" + filter.getElementType().getSimpleName()
                                               + "') isn't able to match the current retrieved data type '" + getCurrentRetrievedDataType().getSimpleName() + "'");
        }

        filters.put(currentRetrieverLevelIndex, filter);
        return this;
    }
    
    @Override
    public <SettingsType extends SerializableSettings> DataRetrieverChainBuilder<DataSourceType> setSettings(SettingsType settings) {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException("The builder hasn't been initialized");
        }
        if (getCurrentRetrieverLevel().getSettingsType() == null) {
            throw new IllegalStateException("The current retrieval level " + getCurrentRetrieverLevel() + " has no settings.");
        }
        
        if (!getCurrentRetrieverLevel().getSettingsType().isAssignableFrom(settings.getClass())) {
            throw new IllegalArgumentException("The given settings (with type '" + settings.getClass().getSimpleName()
                    + "') isn't applicable for the settings type of the current retriever Level '"
                    + getCurrentRetrieverLevel().getSettingsType().getSimpleName() + "'");
        }
        
        this.settings.put(currentRetrieverLevelIndex, settings);
        return this;
    }

    @Override
    public DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<?, ?> resultReceiver) {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException("The builder hasn't been initialized");
        }
        
        if (!resultReceiver.getInputType().isAssignableFrom(getCurrentRetrievedDataType())) {
            throw new IllegalArgumentException("The given result receiver (with input type '" + resultReceiver.getInputType().getSimpleName()
                    + "') isn't able to process the current retrieved data type '" + getCurrentRetrievedDataType().getSimpleName() + "'");
        }

        if (!receivers.containsKey(currentRetrieverLevelIndex)) {
            receivers.put(currentRetrieverLevelIndex, new HashSet<Processor<?, ?>>());
        }
        receivers.get(currentRetrieverLevelIndex).add(resultReceiver);
        return this;
    }

    @Override
    public Class<?> getCurrentRetrievedDataType() {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException("The builder hasn't been initialized");
        }
        
        return retrieverLevels.get(currentRetrieverLevelIndex).getRetrievedDataType();
    }
    
    @Override
    public DataRetrieverLevel<?, ?> getCurrentRetrieverLevel() {
        return hasBeenInitialized() ? retrieverLevels.get(currentRetrieverLevelIndex) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Processor<DataSourceType, ?> build() {
        if (!hasBeenInitialized()) {
            throw new IllegalStateException("The builder hasn't been initialized");
        }
        
        Processor<?, ?> firstRetriever = null;
        for (int retrieverLevelIndex = currentRetrieverLevelIndex; retrieverLevelIndex >= 0; retrieverLevelIndex--) {
            DataRetrieverLevel<?, ?> retrieverLevel = retrieverLevels.get(retrieverLevelIndex);
            firstRetriever = createRetriever(retrieverLevel, firstRetriever, retrieverLevelIndex);
        }
        
        return (Processor<DataSourceType, ?>) firstRetriever;
    }

    public boolean hasBeenInitialized() {
        return currentRetrieverLevelIndex >= 0;
    }
    
    @SuppressWarnings("unchecked")
    private <ResultType> Processor<?, ResultType> createRetriever(DataRetrieverLevel<?, ?> retrieverLevel, Processor<?, ?> previousRetriever, int retrieverLevelIndex) {
        Class<ResultType> retrievedDataType = (Class<ResultType>) retrieverLevel.getRetrievedDataType();
        
        Collection<?> storedResultReceivers = receivers.get(retrieverLevelIndex);
        Collection<Processor<ResultType, ?>> resultReceivers = storedResultReceivers != null ? new ArrayList<Processor<ResultType, ?>>((Collection<Processor<ResultType, ?>>) storedResultReceivers) : new ArrayList<Processor<ResultType, ?>>();
        if (previousRetriever != null) {
            resultReceivers.add((Processor<ResultType, ?>) previousRetriever);
        }
        
        FilterCriterion<ResultType> filter = (FilterCriterion<ResultType>) filters.get(retrieverLevelIndex);
        
        Class<Processor<?, ResultType>> retrieverType = (Class<Processor<?, ResultType>>)(Class<?>) retrieverLevel.getRetrieverType();
        Class<?> settingsType = retrieverLevel.getSettingsType();
        String retrievedDataTypeMessageKey = retrieverLevel.getRetrievedDataTypeMessageKey();
        return createRetriever(retrieverType, retrievedDataType, resultReceivers, filter, settingsType, retrieverLevelIndex, retrievedDataTypeMessageKey);
    }

    private <ResultType> Processor<?, ResultType> createRetriever(Class<Processor<?, ResultType>> retrieverType, Class<ResultType> retrievedDataType,
            Collection<Processor<ResultType, ?>> resultReceivers, FilterCriterion<ResultType> filter, Class<?> settingsType, int retrieverLevelIndex,
            String retrievedDataTypeMessageKey) {
        Constructor<Processor<?, ResultType>> retrieverConstructor = null;
        try {
            if (settingsType == null) {
                retrieverConstructor = retrieverType.getConstructor(ExecutorService.class, Collection.class, int.class, String.class);
            } else {
                retrieverConstructor = retrieverType.getConstructor(ExecutorService.class, Collection.class, settingsType, int.class, String.class);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Couldn't get an usable constructor from the given retrieverType '"
                    + retrieverType.getSimpleName() + "'", e);
        }
        
        SerializableSettings settings = null;
        if (settingsType != null) {
            SerializableSettings storedSettings = this.settings.get(retrieverLevelIndex);
            settings = storedSettings != null ? storedSettings : retrieverLevels.get(retrieverLevelIndex).getDefaultSettings();
        }
        return constructRetriever(retrieverConstructor, retrievedDataType, resultReceivers, filter, settings, settingsType, retrieverLevelIndex, retrievedDataTypeMessageKey);
    }

    private <ResultType> Processor<?, ResultType> constructRetriever(Constructor<Processor<?, ResultType>> retrieverConstructor, Class<ResultType> retrievedDataType,
            Collection<Processor<ResultType, ?>> resultReceivers, FilterCriterion<ResultType> filter, SerializableSettings settings, Class<?> settingsType,
            int retrieverLevelIndex, String retrievedDataTypeMessageKey) {
        try {
            Collection<Processor<ResultType, ?>> retrievalResultReceivers = resultReceivers;
            if (filter != null) {
                Processor<ResultType, ?> filteringProcessor = new ParallelFilteringProcessor<ResultType>(
                        retrievedDataType, executor, resultReceivers, filter);
                retrievalResultReceivers = new ArrayList<>();
                retrievalResultReceivers.add(filteringProcessor);
            }

            
            if (Modifier.isPublic(retrieverConstructor.getModifiers())) {
                // Preventing IllegalAccessExceptions of public constructors due to weird package behaviour
                retrieverConstructor.setAccessible(true);
            }
            
            if (settingsType == null) {
                return retrieverConstructor.newInstance(executor, retrievalResultReceivers, retrieverLevelIndex, retrievedDataTypeMessageKey);
            } else {
                return retrieverConstructor.newInstance(executor, retrievalResultReceivers, settings, retrieverLevelIndex, retrievedDataTypeMessageKey);
            }
        } catch (InstantiationException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Couldn't create a data retriever instance with the constructor "
                                                    + retrieverConstructor.toString(), e);
        }
    }

}
