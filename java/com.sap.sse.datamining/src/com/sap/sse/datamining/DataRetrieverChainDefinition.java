package com.sap.sse.datamining;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * Defines how the data elements of a data type can be retrieved.<br />
 * Represents a list of {@link Processor Processors}, that retrieve the data elements step by step. To build
 * a <code>DataRetrieverChainDefinition</code>, you have to stick to the following steps:
 * <ol>
 *      <li>Call {@link #startWith(Class, Class, String)} to add the first Processor of the chain</li>
 *      <li>Call {@link #addAfter(Class, Class, Class, String)} as often as you want to add the next Processors</li>
 *      <li>Call {@link #endWith(Class, Class, Class, String)} to add the last Processor of the chain</li>
 * </ol>
 * 
 * The Processors in the chain should extend {@link AbstractSimpleRetrievalProcessor} or {@link AbstractRetrievalProcessor}.
 * If not, the processor has to have a constructor with the exact parameter list {@link ExecutorService}, {@link Collection}.
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
    
    public UUID getID();
    
    public Class<DataSourceType> getDataSourceType();
    
    public Class<DataType> getRetrievedDataType();
    
    public String getLocalizedName(Locale locale, ResourceBundleStringMessages stringMessages);

    /**
     * Sets the first {@link Processor} in the chain.<br />
     * The given processor should extend {@link AbstractSimpleRetrievalProcessor} or
     * {@link AbstractRetrievalProcessor}. If not, the processor has to have a constructor with the exact parameter list
     * {@link ExecutorService}, {@link Collection}.
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
     * Sets the next {@link Processor} in the chain. {@link #startWith(Class, Class, String)} has to be called once before you
     * can use this method. Otherwise an exception will be thrown.<br />
     * The given processor should extend {@link AbstractSimpleRetrievalProcessor} or
     * {@link AbstractRetrievalProcessor}. If not, the processor has to have a constructor with the exact parameter list
     * {@link ExecutorService}, {@link Collection}.
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
     * Sets the last {@link Processor} in the chain. {@link #startWith(Class, Class, String)} has to be called once before you
     * can use this method. Otherwise an exception will be thrown.<br />
     * <b>Calling this method completes the chain and no other modifications will be possible!</b><br />
     * The given processor should extend {@link AbstractSimpleRetrievalProcessor} or
     * {@link AbstractRetrievalProcessor}. If not, the processor has to have a constructor with the exact parameter list
     * {@link ExecutorService}, {@link Collection}.
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
     * @return The chain represented as list of the retriever types with additional informations like their
     *         <code>ResultType</code> or message key
     */
    public List<? extends DataRetrieverTypeWithInformation<?, ?>> getDataRetrieverTypesWithInformation();

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
