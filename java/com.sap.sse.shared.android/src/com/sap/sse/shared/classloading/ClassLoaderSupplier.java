package com.sap.sse.shared.classloading;

/**
 * Objects whose class implement this interface can provide a {@link ClassLoader}, usually their own bundle class
 * loader. There are at least two major use cases for something like this currently:
 * <ol>
 * <li>During "master data import" where some scope is imported by one server from another the payload is serialized on
 * the exporting server and de-serialized on the importing server. While de-serializing, all classes of which instances
 * are contained in the payload must be loaded. The class loader of the class implementing the import logic itself will
 * usually not have dependencies to all bundles that contain classes of which instances are contained in the payload.
 * Therefore, bundles contributing to such an import payload must somehow tell the importer about their
 * classloader.</li>
 * <li>During replication, particularly the initial load transmission, it may happen that a replicable's class loader
 * does not have a dependency on all other class loaders needed to de-serialize the replication payload. A typical
 * example (see bug 5687) is the {@code ProgressListener} class whose instances can end up in session objects replicated
 * together with the {@code SecurityService} replicable which doesn't have a dependency on the bundle containing
 * the {@code ProgressListener} class.</li>
 * </ol>
 * This interface can be subtyped by marker interfaces which are then used to register such suppliers for a specific purpose
 * in the OSGi service registry. See, e.g., {@code MasterDataImportClassLoaderService}. It can be used in conjunction with
 * 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ClassLoaderSupplier {
    ClassLoader getClassLoader();
}
