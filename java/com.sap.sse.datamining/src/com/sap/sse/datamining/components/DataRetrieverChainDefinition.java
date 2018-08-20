package com.sap.sse.datamining.components;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * Defines how the data elements of a data type can be retrieved.<br />
 * Represents a list of {@link Processor Processors}, that retrieve the data elements step by step. To build
 * a <code>DataRetrieverChainDefinition</code>, you have to stick to the following steps:
 * <ol>
 *      <li>Call {@link #startWith(Class, Class, String) start} to add the first Processor of the chain</li>
 *      <li>Call {@link #addAfter(Class, Class, Class, String) addAfter} as often as you want to add the next Processors</li>
 *      <li>Call {@link #endWith(Class, Class, Class, String) endWith} to add the last Processor of the chain</li>
 * </ol>
 * 
 * The Processors in the chain should extend {@link AbstractRetrievalProcessor}.
 * If not, the processor has to have a constructor with the exact parameter list
 * ({@link ExecutorService}, {@link Collection}, <code>int</code>) or ({@link ExecutorService}, {@link Collection}, <code>SettingsType</code>, <code>int</code>).
 * Otherwise an exception will be thrown, when you call one of the methods above.<br/>
 * To create a data retriever chain defined by a <code>DataRetrieverChainDefinition</code>, call {@link #startBuilding(ExecutorService)}
 * which returns an instance of {@link DataRetrieverChainBuilder}.
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <DataSourceType> The type of the data source and the <code>InputType</code> of the first Processor.
 * @param <DataType> The type of the retrieved data elements and the <code>ResultType</code> of the last Processor.
 */
public interface DataRetrieverChainDefinition<DataSourceType, DataType> {
    
    public Class<DataSourceType> getDataSourceType();
    
    public Class<DataType> getRetrievedDataType();
    
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages);

    /**
     * Sets the first {@link Processor} in the chain.<br />
     * The given processor should extend {@link AbstractRetrievalProcessor}.
     * If not, the processor has to have a constructor with the exact parameter list
     * {@link ExecutorService}, {@link Collection}, <code>int</code>.
     * 
     * @param retrieverType The type of the first processor in the chain
     * @param retrievedDataType The <code>ResultType</code> of the <code>retrieverType</code>
     * @param retrievedDataTypeMessageKey The message key to describe the <code>retrieverType</code>
     * 
     * @throws UnsupportedOperationException If the chain has already been started
     * @throws IllegalArgumentException If the given <code>retrieverType</code> has no usable constructor
     */
    public <ResultType> void startWith(Class<? extends Processor<DataSourceType, ResultType>> retrieverType,
            Class<ResultType> retrievedDataType, String retrievedDataTypeMessageKey);

    /**
     * Sets the first {@link Processor} in the chain.<br />
     * The given processor should extend {@link AbstractRetrievalProcessor} and needs a constructor with the exact
     * parameter list {@link ExecutorService}, {@link Collection}, <code>settings</code>, <code>int</code>.
     * 
     * @param retrieverType
     *            The type of the first processor in the chain
     * @param retrievedDataType
     *            The <code>ResultType</code> of the <code>retrieverType</code>
     * @param settingsType
     *            The type of the settings of the processor; if not {@code null}, the {@code lastRetrieverType} must
     *            offer a constructor with the following signature: {@code lastRetrieverType(}{@link ExecutorService},
     *            {@link Collection}, {@code settingsType, int)}, otherwise a setting-less constructor will be used,
     *            with signature {@code lastRetrieverType(}{@link ExecutorService}, {@link Collection}, {@code int)}
     * @param defaultSettings
     *            The default settings for the first processor in the chain
     * @param retrievedDataTypeMessageKey
     *            The message key to describe the <code>retrieverType</code>
     * @throws UnsupportedOperationException
     *             If the chain has already been started
     * @throws IllegalArgumentException
     *             If the given <code>retrieverType</code> has no usable constructor
     * @throws NullPointerException
     *             If the given <code>defaultSettings</code> are <code>null</code>
     */
    public <ResultType, SettingsType extends SerializableSettings> void startWith(Class<? extends Processor<DataSourceType, ResultType>> retrieverType,
            Class<ResultType> retrievedDataType, Class<SettingsType> settingsType, SettingsType defaultSettings, String retrievedDataTypeMessageKey);

    /**
     * Sets the next {@link Processor} in the chain. {@link #startWith(Class, Class, String)} has to be called once before you
     * can use this method. Otherwise an exception will be thrown.<br />
     * The given processor should extend {@link AbstractRetrievalProcessor}.
     * If not, the processor has to have a constructor with the exact parameter list
     * {@link ExecutorService}, {@link Collection}, <code>int</code>.
     * 
     * @param lastAddedRetrieverType The processor that has been added before the <code>nextRetrieverType</code>
     * @param nextRetrieverType The next processor in the chain
     * @param retrievedDataType The <code>ResultType</code> of the <code>nextRetrieverType</code>
     * @param retrievedDataTypeMessageKey The message key to describe the <code>nextRetrieverType</code>
     * 
     * @throws UnsupportedOperationException If the chain hasn't been started yet
     * @throws UnsupportedOperationException If the chain is already complete
     * @throws IllegalArgumentException If the given <code>lastAddedRetrieverType</code>  isn't correct
     * @throws IllegalArgumentException If the given <code>retrieverType</code> has no usable constructor
     */
    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType> void
           addAfter(Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
                     Class<? extends Processor<NextInputType, NextResultType>> nextRetrieverType,
                     Class<NextResultType> retrievedDataType, String retrievedDataTypeMessageKey);

    /**
     * Sets the next {@link Processor} in the chain. {@link #startWith(Class, Class, String)} has to be called once before you
     * can use this method. Otherwise an exception will be thrown.<br />
     * The given processor should extend {@link AbstractRetrievalProcessor} and needs a constructor
     * with the exact parameter list {@link ExecutorService}, {@link Collection}, <code>settings</code>, <code>int</code>.
     * 
     * @param lastAddedRetrieverType The processor that has been added before the <code>nextRetrieverType</code>
     * @param nextRetrieverType The next processor in the chain
     * @param retrievedDataType The <code>ResultType</code> of the <code>nextRetrieverType</code>
     * @param settingsType The type of the settings of the processor
     * @param defaultSettings The default settings for the next processor in the chain
     * @param retrievedDataTypeMessageKey The message key to describe the <code>nextRetrieverType</code>
     * @throws UnsupportedOperationException If the chain hasn't been started yet
     * @throws UnsupportedOperationException If the chain is already complete
     * @throws IllegalArgumentException If the given <code>lastAddedRetrieverType</code>  isn't correct
     * @throws IllegalArgumentException If the given <code>retrieverType</code> has no usable constructor
     * @throws NullPointerException If the given <code>defaultSettings</code> are <code>null</code>
     */
    public <NextInputType, NextResultType, PreviousInputType, PreviousResultType extends NextInputType, SettingsType extends SerializableSettings> void
           addAfter(Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
                     Class<? extends Processor<NextInputType, NextResultType>> nextRetrieverType,
                     Class<NextResultType> retrievedDataType, Class<SettingsType> settingsType,
                     SettingsType defaultSettings, String retrievedDataTypeMessageKey);

    /**
     * Sets the last {@link Processor} in the chain. {@link #startWith(Class, Class, String)} has to be called once before you
     * can use this method. Otherwise an exception will be thrown.<br />
     * <b>Calling this method completes the chain and no other modifications will be possible!</b><br />
     * The given processor should extend {@link AbstractRetrievalProcessor}.
     * If not, the processor has to have a constructor with the exact parameter list
     * {@link ExecutorService}, {@link Collection}., <code>int</code>
     * 
     * @param lastAddedRetrieverType The processor that has been added before the <code>lastRetrieverType</code>
     * @param lastRetrieverType The last processor in the chain
     * @param retrievedDataType The <code>ResultType</code> of the <code>lastRetrieverType</code>
     * @param retrievedDataTypeMessageKey The message key to describe the <code>lastRetrieverType</code>
     * 
     * @throws UnsupportedOperationException If the chain hasn't been started yet
     * @throws UnsupportedOperationException If the chain is already complete
     * @throws IllegalArgumentException If the given <code>lastAddedRetrieverType</code>  isn't correct
     * @throws IllegalArgumentException If the given <code>retrieverType</code> has no usable constructor
     */
    public <NextInputType, PreviousInputType, PreviousResultType extends NextInputType> void
           endWith(Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
                     Class<? extends Processor<NextInputType, DataType>> lastRetrieverType,
                     Class<DataType> retrievedDataType, String retrievedDataTypeMessageKey);

    /**
     * Sets the last {@link Processor} in the chain. {@link #startWith(Class, Class, String)} has to be called once
     * before you can use this method. Otherwise an exception will be thrown.<br />
     * <b>Calling this method completes the chain and no other modifications will be possible!</b><br />
     * The given processor should extend {@link AbstractRetrievalProcessor} and needs a constructor with the exact
     * parameter list {@link ExecutorService}, {@link Collection}, <code>settings</code>, <code>int</code>.
     * 
     * @param lastAddedRetrieverType
     *            The processor that has been added before the <code>lastRetrieverType</code>
     * @param lastRetrieverType
     *            The last processor in the chain
     * @param retrievedDataType
     *            The <code>ResultType</code> of the <code>lastRetrieverType</code>
     * @param settingsType
     *            The type of the settings of the processor; if not {@code null}, the {@code lastRetrieverType} must
     *            offer a constructor with the following signature: {@code lastRetrieverType(}{@link ExecutorService}, {@link Collection},
     *            {@code settingsType, int)}, otherwise a setting-less constructor will be used, with signature
     *            {@code lastRetrieverType(}{@link ExecutorService}, {@link Collection},
     *            {@code int)}
     * @param defaultSettings
     *            The default settings for the last processor in the chain
     * @param retrievedDataTypeMessageKey
     *            The message key to describe the <code>lastRetrieverType</code>
     * @throws UnsupportedOperationException
     *             If the chain hasn't been started yet
     * @throws UnsupportedOperationException
     *             If the chain is already complete
     * @throws IllegalArgumentException
     *             If the given <code>lastAddedRetrieverType</code> isn't correct
     * @throws IllegalArgumentException
     *             If the given <code>retrieverType</code> has no usable constructor
     * @throws NullPointerException
     *             If the given <code>defaultSettings</code> are <code>null</code>
     */
    public <NextInputType, PreviousInputType, PreviousResultType extends NextInputType, SettingsType extends SerializableSettings> void
           endWith(Class<? extends Processor<PreviousInputType, PreviousResultType>> lastAddedRetrieverType,
                     Class<? extends Processor<NextInputType, DataType>> lastRetrieverType,
                     Class<DataType> retrievedDataType, Class<SettingsType> settingsType,
                     SettingsType defaultSettings, String retrievedDataTypeMessageKey);
    
    /**
     * @return The chain represented as list of the retriever levels with additional informations like their
     *         <code>RetrieverType</code>, <code>ResultType</code> or message key
     */
    public List<? extends DataRetrieverLevel<?, ?>> getDataRetrieverLevels();

    /**
     * @param levelIndex
     * @return The retriever level for the given index or <code>null</code>, if the index is out of bounds.
     */
    public DataRetrieverLevel<?, ?> getDataRetrieverLevel(int levelIndex);

    /**
     * Returns a {@link DataRetrieverChainBuilder}, that is used to construct the chain. {@link #endWith(Class, Class, Class, String)}
     * has to be called before you can use this method. Otherwise an exception will be thrown.
     * 
     * @return a {@link DataRetrieverChainBuilder}, that is used to construct the chain
     * 
     * @throws UnsupportedOperationException If the chain hasn't been completed yet
     */
    public DataRetrieverChainBuilder<DataSourceType> startBuilding(ExecutorService executor);

}
