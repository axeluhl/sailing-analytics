package com.sap.sse.datamining.components;

public interface Processor<ElementType> {
	
	public void onElement(ElementType element);

}
