package com.sap.sailing.server.impl.preferences;

import org.osgi.framework.BundleContext;

import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sse.security.UserStore;

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
