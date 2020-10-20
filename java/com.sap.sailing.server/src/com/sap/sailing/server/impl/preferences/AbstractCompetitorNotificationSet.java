package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.interfaces.UserStore;

/**
 * {@link PreferenceObjectBasedNotificationSet} for associations of {@link Competitor}'s {@link Competitor#getId() IDs}
 * in {@link String} form to a set of users to notify about specific events for the {@link Competitor}. Subclasses
 * define the concrete case of notification based on the flags in {@link CompetitorNotificationPreference} via
 * implementing {@link #shouldNotifyFor(CompetitorNotificationPreference)}.
 */
public abstract class AbstractCompetitorNotificationSet
        extends PreferenceObjectBasedNotificationSet<CompetitorNotificationPreferences, String> {
    public AbstractCompetitorNotificationSet(UserStore userStore, CompetitorAndBoatStore competitorAndBoatStore) {
        super(CompetitorNotificationPreferences.PREF_NAME, userStore);
    }

    /**
     * Constructor used to automatically track {@link UserStore} and {@link CompetitorAndBoatStore} as OSGi service.
     */
    public AbstractCompetitorNotificationSet(BundleContext bundleContext) {
        super(CompetitorNotificationPreferences.PREF_NAME, bundleContext);
    }

    @Override
    protected Collection<String> calculateObjectsToNotify(CompetitorNotificationPreferences preference) {
        final Set<String> result = new HashSet<>();
        for (CompetitorNotificationPreference pref : preference.getCompetitors()) {
            if (shouldNotifyFor(pref)) {
                final String competitorIdAsString = pref.getCompetitorIdAsString();
                if (competitorIdAsString != null) {
                    result.add(competitorIdAsString);
                }
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(CompetitorNotificationPreference pref);
}
