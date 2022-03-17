package com.sap.sse.classloading;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.shared.classloading.ClassLoaderRegistry;
import com.sap.sse.shared.classloading.ClassLoaderSupplier;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Use an instance of this type to track the appearing and disappearing of {@link ClassLoaderSupplier}s of
 * a specific subtype, adding/removing those suppliers to a {@link ClassLoaderRegistry} provided to this tracker
 * at construction time. Create a service tracker using this customizer, e.g., as follows:
 * <pre>
 *     masterDataImportClassLoaderServiceTracker =
 *         ServiceTrackerFactory.createAndOpen(context, MasterDataImportClassLoaderService.class,
 *                                             new ServiceTrackerCustomizerForClassLoaderSupplierRegistrations<>(context, classLoaderRegistry);
 * </pre>
 * The {@link ClassLoaderRegistry#getCombinedMasterDataClassLoader()} method can then be used on the class loader registry
 * provided in order to obtain an up-to-date combined class loader that represents the union of all class loaders
 * supplied by the suppliers tracked by this tracker.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <C>
 */
public class ServiceTrackerCustomizerForClassLoaderSupplierRegistrations<C extends ClassLoaderSupplier>
        implements ServiceTrackerCustomizer<C, C> {
    private final BundleContext context;
    private ClassLoaderRegistry classLoaderRegistry;
    
    /**
     * Creates and returns a service tracker for services of type {@code clazz} which is some {@link ClassLoaderSupplier} subtype.
     * The service tracker uses a "customizer" (basically a callback) which keeps the {@code classLoaderRegistry} up to date
     * with the class loaders supplied by the services tracked.
     */
    public static <C extends ClassLoaderSupplier> ServiceTracker<C, C> createClassLoaderSupplierServiceTracker(BundleContext context, Class<C> clazz, ClassLoaderRegistry classLoaderRegistry) {
        // start watching out for MasterDataImportClassLoaderService instances in the OSGi service registry and manage
        // the combined class loader accordingly:
        final ServiceTrackerCustomizerForClassLoaderSupplierRegistrations<C> serviceTrackerCustomizer =
                new ServiceTrackerCustomizerForClassLoaderSupplierRegistrations<>(context, classLoaderRegistry);
        ServiceTracker<C, C> serviceTracker = ServiceTrackerFactory.createAndOpen(context, clazz, serviceTrackerCustomizer);
        final ServiceReference<C>[] serviceReferences = serviceTracker.getServiceReferences();
        if (serviceReferences != null) {
            for (final ServiceReference<C> mdiClassLoaderService : serviceReferences) {
                serviceTrackerCustomizer.addingService(mdiClassLoaderService);
            }
        }
        return serviceTracker;
    }

    public ServiceTrackerCustomizerForClassLoaderSupplierRegistrations(BundleContext context, ClassLoaderRegistry classLoaderRegistry) {
        this.context = context;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Override
    public C addingService(ServiceReference<C> reference) {
        C service = context.getService(reference);
        classLoaderRegistry.addClassLoader(service.getClassLoader());
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<C> reference, C service) {
    }

    @Override
    public void removedService(ServiceReference<C> reference, C service) {
        classLoaderRegistry.removeClassLoader(service.getClassLoader());
    }
}
