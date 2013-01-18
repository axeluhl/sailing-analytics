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
	/**
	 * Should be called before accessing any collection.
	 * 
	 * @param registerForThisClass
	 *            The class to register the collection for. This might be a
	 *            placeholder Interface that is used by multiple classes
	 *            accessing one and the same collection, demonstrating that they
	 *            have knowledge of each other. Also, this provides safety in
	 *            the case that a bundle is restarted, registering the
	 *            collection name more than once.
	 * @param collectionName
	 * @throws AlreadyRegisteredException
	 *             Is thrown if the collection name has already been registered
	 *             for another class. This shouldn't happen in a productive
	 *             version, but should make aware of the problem while
	 *             developing.
	 */
	void registerExclusively(Class<?> registerForThisClass,
			String collectionName) throws AlreadyRegisteredException;
}
