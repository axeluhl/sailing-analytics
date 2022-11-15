package com.sap.sailing.server.impl.preferences;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sse.security.interfaces.UserStore;

/**
 * Notification set that defines the association of a {@link BoatClass} to the set of Users that need to be notified
 * whenever a race is known to start in the near future for a {@link BoatClass}.
 */
public class BoatClassUpcomingRaceNotificationSet extends AbstractBoatClassNotificationSet {

    public BoatClassUpcomingRaceNotificationSet(UserStore store) {
        super(store);
    }

    /**
     * Constructor used to automatically track {@link UserStore} as OSGi service.
     */
    public BoatClassUpcomingRaceNotificationSet(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    protected boolean shouldNotifyFor(BoatClassNotificationPreference pref) {
        return pref.isNotifyAboutUpcomingRaces();
    }
}
