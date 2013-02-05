package com.sap.sailing.server.gateway.serialization.filter;

public interface Filter<ElementType> {
	
	public class NoFilter<Element> implements Filter<Element> {
		@Override
		public boolean isFiltered(Element object) {
			return false;
		}
	}

	public boolean isFiltered(ElementType object);
	
}
