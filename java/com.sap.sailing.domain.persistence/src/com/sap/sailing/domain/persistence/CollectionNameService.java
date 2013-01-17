package com.sap.sailing.domain.persistence;

/**
 * A collection name can only be registered once using this service. This
 * ensures that no hidden conflicts are introduced, if somewhere two identical
 * collection names were unknowingly to be used.
 * 
 * This solves the problem of not having to hard-code all collection names
 * centrally in an {@link Enum}, but still gives safety against hidden conflicts
 * that might be difficult to find otherwise.
 * 
 * {@link CollectionNameService#registerExclusively(String)} should therefore be
 * called before any access to a collection. This might happen for example in
 * the constructor of a bundle-specific domain factory singleton.
 * 
 * @author Fredrik Teschke
 * 
 */
public interface CollectionNameService {
	void registerExclusively(String collectionName)
			throws AlreadyRegisteredException;
}
