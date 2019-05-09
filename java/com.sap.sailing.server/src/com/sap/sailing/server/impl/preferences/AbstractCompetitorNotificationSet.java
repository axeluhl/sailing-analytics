package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.interfaces.UserStore;

/**
 * {@link PreferenceObjectBasedNotificationSet} for associations of {@link Competitor} to a set of users to notify about
 * specific events for the {@link Competitor}. Subclasses define the concrete case of notification based on the flags in
 * {@link CompetitorNotificationPreference} via implementing {@link #shouldNotifyFor(CompetitorNotificationPreference)}.
 */
public abstract class AbstractCompetitorNotificationSet
        extends PreferenceObjectBasedNotificationSet<CompetitorNotificationPreferences, Competitor> {

    public AbstractCompetitorNotificationSet(UserStore store) {
        super(CompetitorNotificationPreferences.PREF_NAME, store);
    }

    /**
     * Constructor used to automatically track {@link UserStore} as OSGi service.
     */
    public AbstractCompetitorNotificationSet(BundleContext bundleContext) {
        super(CompetitorNotificationPreferences.PREF_NAME, bundleContext);
    }

    @Override
    protected Collection<Competitor> calculateObjectsToNotify(CompetitorNotificationPreferences preference) {
        Set<Competitor> result = new HashSet<>();
        for (CompetitorNotificationPreference pref : preference.getCompetitors()) {
            if (shouldNotifyFor(pref)) {
                result.add(pref.getCompetitor());
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(CompetitorNotificationPreference pref);

}
