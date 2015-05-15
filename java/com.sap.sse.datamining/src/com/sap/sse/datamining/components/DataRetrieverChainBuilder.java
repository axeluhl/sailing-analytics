package com.sap.sse.datamining.components;


/**
 * Used to construct a chain of retriever processors step by step. Allows to set the filter and result
 * receivers for each element in the chain, before the chain is constructed.
 * 
 * @author Lennart Hensler (D054527)
 *
 * @param <DataSourceType> The type of the data source and the <code>InputType</code> of the first Processor.
 */
public interface DataRetrieverChainBuilder<DataSourceType> {
    
    /**
     * @return <code>true</code>, if there's a next data retriever in the chain.
     */
    public boolean canStepFurther();
    
    /**
     * Steps to the next data retriever in the chain.
     * 
     * @throws IllegalStateException if {@link #canStepFurther()} would return <code>false</code>.
     */
    public DataRetrieverChainBuilder<DataSourceType> stepFurther();

    /**
     * @throws IllegalStateException if {@link #stepFurther()} has not yet been called.
     * @return The retrieved data type of the data retriever of the current level.
     */
    public Class<?> getCurrentRetrievedDataType();
    
    /**
     * @return The current retriever level or <code>-1</code> if {@link #stepFurther()} has not yet been called.
     */
    public int getCurrentRetrieverLevel();

    /**
     * Sets the filter for the data retriever of the current level.
     * 
     * @throws IllegalStateException if {@link #stepFurther()} has not yet been called.
     * @throws IllegalArgumentException if the filters <code>ElementType</code> doesn't match the
     *         {@link #getCurrentRetrievedDataType() current retrieved data type}.
     */
    public DataRetrieverChainBuilder<DataSourceType> setFilter(FilterCriterion<?> filter);
    
    /**
     * Adds a result receiver to the data retriever of the current level.
     * 
     * @throws IllegalStateException if {@link #stepFurther()} has not yet been called.
     * @throws IllegalArgumentException if the result receivers {@link Processor#getInputType() <code>InputType</code>} doesn't match the
     *         {@link #getCurrentRetrievedDataType() current retrieved data type}.
     */
    public DataRetrieverChainBuilder<DataSourceType> addResultReceiver(Processor<?, ?> resultReceiver);

    /**
     * Builds the configured data retriever chain.
     * 
     * @throws IllegalStateException if {@link #stepFurther()} has not yet been called.
     * @return The first processor of the built data retriever chain.
     */
    public Processor<DataSourceType, ?> build();

}
