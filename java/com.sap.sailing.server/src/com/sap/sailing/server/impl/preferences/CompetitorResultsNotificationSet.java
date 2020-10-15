package com.sap.sailing.server.impl.preferences;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreference;
import com.sap.sse.security.interfaces.UserStore;

/**
 * Notification set that defines the association of a {@link Competitor} to the set of Users that need to be notified
 * whenever new results are available for a {@link Competitor}.
 */
public class CompetitorResultsNotificationSet extends AbstractCompetitorNotificationSet {

    public CompetitorResultsNotificationSet(UserStore store, CompetitorAndBoatStore competitorAndBoatStore) {
        super(store, competitorAndBoatStore);
    }

    /**
     * Constructor used to automatically track {@link UserStore} and {@link CompetitorAndBoatStore} as OSGi service.
     */
    public CompetitorResultsNotificationSet(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    protected boolean shouldNotifyFor(CompetitorNotificationPreference pref) {
        return pref.isNotifyAboutResults();
    }

}
