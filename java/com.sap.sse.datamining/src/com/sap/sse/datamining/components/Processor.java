package com.sap.sse.datamining.components;

public interface Processor<InputType> {
	
	public void onElement(InputType element);

}
