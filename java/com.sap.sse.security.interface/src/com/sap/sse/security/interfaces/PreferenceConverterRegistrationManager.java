package com.sap.sse.security.interfaces;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.Stoppable;

/**
 * Automatically registers/removes {@link PreferenceConverter} instances found in the OSGi service registry on the given
 * {@link UserStore}.
 * <p>
 *
 * {@link PreferenceConverter} found are required to have a property with the key
 * {@link PreferenceConverter#KEY_PARAMETER_NAME} defining the preference key the converter should be registered with.
 */
public class PreferenceConverterRegistrationManager implements Stoppable {
    private static final Logger logger = Logger.getLogger(PreferenceConverterRegistrationManager.class.getName());

    private final BundleContext context;
    private final UserStore userStore;

    private final ServiceTracker<PreferenceConverter<?>, PreferenceConverter<?>> tracker;

    /**
     * @param bundleContext
     *            the {@link BundleContext} needed to track {@link PreferenceConverter} instances that need to be
     *            registered a the given UserStore.
     * @param userStore
     *            the {@link UserStore} to register {@link PreferenceConverter} at.
     */
    public PreferenceConverterRegistrationManager(BundleContext bundleContext, UserStore userStore) {
        this.context = bundleContext;
        this.userStore = userStore;
        tracker = createTracker(bundleContext);
        tracker.open();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ServiceTracker<PreferenceConverter<?>, PreferenceConverter<?>> createTracker(BundleContext bundleContext) {
        return new ServiceTracker<PreferenceConverter<?>, PreferenceConverter<?>>(bundleContext,
                (Class) PreferenceConverter.class, new Cutomizer());
    }

    @Override
    public void stop() {
        tracker.close();
    }

    private class Cutomizer implements ServiceTrackerCustomizer<PreferenceConverter<?>, PreferenceConverter<?>> {

        @Override
        public PreferenceConverter<?> addingService(ServiceReference<PreferenceConverter<?>> reference) {
            final String preferenceKey = (String) reference.getProperty(PreferenceConverter.KEY_PARAMETER_NAME);
            final PreferenceConverter<?> converter = context.getService(reference);
            if (preferenceKey == null) {
                logger.log(Level.SEVERE,
                        "Found PreferenceConverter \"" + converter + "\" in OSGi registry without property "
                                + PreferenceConverter.KEY_PARAMETER_NAME + " defining the preference key");
            } else {
                logger.log(Level.FINE, "Registering PreferenceConverter \"" + converter
                        + "\" found as OSGi service with key \"" + preferenceKey + "\"");
                userStore.registerPreferenceConverter(preferenceKey, converter);
            }
            return converter;
        }

        @Override
        public void modifiedService(ServiceReference<PreferenceConverter<?>> reference,
                PreferenceConverter<?> service) {
            // Should we do anything here? the preference key could have changed, but does this make any sense?
        }

        @Override
        public void removedService(ServiceReference<PreferenceConverter<?>> reference, PreferenceConverter<?> service) {
            String preferenceKey = (String) reference.getProperty(PreferenceConverter.KEY_PARAMETER_NAME);
            if (preferenceKey != null) {
                logger.log(Level.FINE,
                        "Removing PreferenceConverter \"" + service + "\" with key \"" + preferenceKey + "\"");
                userStore.removePreferenceConverter(preferenceKey);
            }
        }
    }
}
