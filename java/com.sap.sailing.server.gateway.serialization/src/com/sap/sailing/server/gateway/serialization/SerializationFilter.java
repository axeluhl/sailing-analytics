package com.sap.sailing.server.gateway.serialization;

public interface SerializationFilter<T> {
	
	public class NoFilter<T> implements SerializationFilter<T> {
		@Override
		public boolean isFiltered(T object) {
			return false;
		}
	}

	public boolean isFiltered(T object);
	
}
