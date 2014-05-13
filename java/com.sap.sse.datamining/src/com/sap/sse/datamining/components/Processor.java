package com.sap.sse.datamining.components;

import com.sap.sse.datamining.AdditionalResultDataBuilder;

public interface Processor<InputType> {

    public void onElement(InputType element);

    /**
     * Tells this Processor, that there will be no incoming data.<br />
     * The called Processor will finish his work and call <code>finish()</code> on all subsequent processors.
     * 
     * @throws InterruptedException
     */
    public void finish() throws InterruptedException;

    public void abort();

    /**
     * Takes a result builder and fills it with its additional data and the data of its result receivers.
     * @return The builder filled with the additional data of all processors in the chain. It can be used to
     *         construct the additional data of the executed processor chain. 
     */
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder);

}
