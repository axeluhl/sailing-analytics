package com.sap.sse.shared.classloading;

import com.sap.sse.shared.classloading.impl.ClassLoaderRegistryImpl;

/**
 * Manages a collection of class loaders where for their combined set a combined class loader can be
 * requested. This pattern can be used, e.g., to manage class loaders to be used for de-serialization
 * of objects for master data import or for initial load de-serialization of a replicable. Managing
 * the adding/removing of such class loaders may, e.g., be supported by a service tracker such as
 * {@code ServiceTrackerForInitialLoadClassLoaderRegistration}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ClassLoaderRegistry {
    /**
     * Creates a new instance whose class implements this interface, starting out with an empty set
     * of class loaders.
     */
    static ClassLoaderRegistry createInstance() {
        return new ClassLoaderRegistryImpl();
    }
    
    void addClassLoader(ClassLoader classLoader);

    void removeClassLoader(ClassLoader classLoader);

    ClassLoader getCombinedMasterDataClassLoader();
}
