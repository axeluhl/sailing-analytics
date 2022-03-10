package com.sap.sse.replication;

import com.sap.sse.replication.interfaces.impl.ServiceTrackerForInitialLoadClassLoaderRegistration;

/**
 * Manages a collection of class loaders where for their combined set a combined class loader can be
 * requested. This pattern can be used, e.g., to manage class loaders to be used for de-serialization
 * of objects for master data import or for initial load de-serialization of a replicable. Managing
 * the adding/removing of such class loaders may, e.g., be supported by a service tracker such as
 * {@link ServiceTrackerForInitialLoadClassLoaderRegistration}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface InitialLoadClassLoaderRegistry {
    void addMasterDataClassLoader(ClassLoader classLoader);

    void removeMasterDataClassLoader(ClassLoader classLoader);

    ClassLoader getCombinedMasterDataClassLoader();
}
