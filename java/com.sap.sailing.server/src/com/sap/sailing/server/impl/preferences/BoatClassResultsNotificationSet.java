package com.sap.sailing.server.impl.preferences;

import org.osgi.framework.BundleContext;

import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sse.security.UserStore;

public class BoatClassResultsNotificationSet extends AbstractBoatClassNotificationSet {

    public BoatClassResultsNotificationSet(UserStore store) {
        super(store);
    }

    /**
     * Constructor used to automatically track {@link UserStore} as OSGi service.
     */
    public BoatClassResultsNotificationSet(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    protected boolean shouldNotifyFor(BoatClassNotificationPreference pref) {
        return pref.isNotifyAboutResults();
    }
}
