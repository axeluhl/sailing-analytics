package com.sap.sse.datamining.components;

public interface Processor<InputType> {
	
	public void onElement(InputType element);
	
	/**
	 * Tells this Processor, that there will be no incoming data.<br />
	 * The called Processor will finish his work and call <code>finish()</code> on all subsequent processors.
	 * @throws InterruptedException 
	 */
	public void finish() throws InterruptedException;

}
