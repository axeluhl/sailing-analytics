package com.sap.sailing.domain.persistence.devices;


public interface MongoPersistenceHandler<T> {
	Object store(T object) throws IllegalArgumentException;
	T load(Object input);
}
