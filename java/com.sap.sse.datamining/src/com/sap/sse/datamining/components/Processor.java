package com.sap.sse.datamining.components;

import com.sap.sse.datamining.AdditionalResultDataBuilder;

public interface Processor<InputType, ResultType> {

    /**
     * Processes the given element and forwards the result.
     * @param element The element to process.
     */
    public void processElement(InputType element);

    /**
     * This method handles the throwing of Throwables, so they don't get lost because of the 
     * multi-threading.<br>
     * The standard implementation is forwarding them to the last processor,
     * that collects the failures, until the processing is finished. Than the failures will be
     * handled.
     * @param failure The thrown failure.
     */
    void onFailure(Throwable failure);

    /**
     * Tells this Processor, that there won't be data incoming anymore.<br />
     * The called Processor will finish his work and call <code>finish()</code> on all subsequent processors.
     * 
     * @throws InterruptedException
     */
    public void finish() throws InterruptedException;

    /**
     * Aborts the processing immediately. The result will be <code>null</code>, incomplete or undefined.<br />
     * To shut down the process cleanly use {@link #finish()}.
     */
    public void abort();
    
    public Class<InputType> getInputType();
    
    public Class<ResultType> getResultType();

    /**
     * Takes a result builder and fills it with its additional data and the data of its result receivers.
     * @return The builder filled with the additional data of all processors in the chain. It can be used to
     *         construct the additional data of the executed processor chain. 
     */
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder);

}
