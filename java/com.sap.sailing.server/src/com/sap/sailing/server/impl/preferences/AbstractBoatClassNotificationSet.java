package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.UserStore;

/**
 * {@link PreferenceObjectBasedNotificationSet} for associations of {@link BoatClass} to a set of users to notify about
 * specific events for the {@link BoatClass}. Subclasses define the concrete case of notification based on the flags in
 * {@link BoatClassNotificationPreference} via implementing {@link #shouldNotifyFor(BoatClassNotificationPreference)}.
 */
public abstract class AbstractBoatClassNotificationSet
        extends PreferenceObjectBasedNotificationSet<BoatClassNotificationPreferences, BoatClass> {

    public AbstractBoatClassNotificationSet(UserStore store) {
        super(BoatClassNotificationPreferences.PREF_NAME, store);
    }

    /**
     * Constructor used to automatically track {@link UserStore} as OSGi service.
     */
    public AbstractBoatClassNotificationSet(BundleContext bundleContext) {
        super(BoatClassNotificationPreferences.PREF_NAME, bundleContext);
    }

    @Override
    protected Collection<BoatClass> calculateObjectsToNotify(BoatClassNotificationPreferences preference) {
        Set<BoatClass> result = new HashSet<>();
        for (BoatClassNotificationPreference pref : preference.getBoatClasses()) {
            if (shouldNotifyFor(pref)) {
                result.add(pref.getBoatClass());
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(BoatClassNotificationPreference pref);

}
