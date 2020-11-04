package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.StoreServiceTrackerCustomizer;
import com.sap.sse.security.interfaces.UserStore;

/**
 * {@link PreferenceObjectBasedNotificationSet} for associations of {@link Competitor} to a set of users to notify about
 * specific events for the {@link Competitor}. Subclasses define the concrete case of notification based on the flags in
 * {@link CompetitorNotificationPreference} via implementing {@link #shouldNotifyFor(CompetitorNotificationPreference)}.
 */
public abstract class AbstractCompetitorNotificationSet
        extends PreferenceObjectBasedNotificationSet<CompetitorNotificationPreferences, Competitor> {
    private static final Logger logger = Logger.getLogger(AbstractCompetitorNotificationSet.class.getName());
    private CompetitorAndBoatStore competitorAndBoatStore;
    private ServiceTracker<CompetitorAndBoatStore, CompetitorAndBoatStore> tracker;

    public AbstractCompetitorNotificationSet(UserStore userStore, CompetitorAndBoatStore competitorAndBoatStore) {
        super(CompetitorNotificationPreferences.PREF_NAME, userStore);
        this.competitorAndBoatStore = competitorAndBoatStore;
    }

    /**
     * Constructor used to automatically track {@link UserStore} and {@link CompetitorAndBoatStore} as OSGi service.
     */
    public AbstractCompetitorNotificationSet(BundleContext bundleContext) {
        super(CompetitorNotificationPreferences.PREF_NAME, bundleContext);
        if (bundleContext == null) {
            this.tracker = null;
        } else {
            this.tracker = new ServiceTracker<CompetitorAndBoatStore, CompetitorAndBoatStore>(bundleContext,
                    CompetitorAndBoatStore.class,
                    new StoreServiceTrackerCustomizer<CompetitorAndBoatStore>(bundleContext, logger) {
                        @Override
                        protected void setStore(CompetitorAndBoatStore store) {
                            AbstractCompetitorNotificationSet.this.competitorAndBoatStore = store;
                        }
                        @Override
                        protected void removeStore() {
                            AbstractCompetitorNotificationSet.this.competitorAndBoatStore = null;
                        }
                        @Override
                        protected CompetitorAndBoatStore getStore() {
                            return AbstractCompetitorNotificationSet.this.competitorAndBoatStore;
                        }
            });
            this.tracker.open();
        }
    }

    @Override
    protected Collection<Competitor> calculateObjectsToNotify(CompetitorNotificationPreferences preference) {
        Set<Competitor> result = new HashSet<>();
        for (CompetitorNotificationPreference pref : preference.getCompetitors()) {
            if (shouldNotifyFor(pref)) {
                String competitorId = pref.getCompetitorId();
                DynamicCompetitor competitor = competitorAndBoatStore.getExistingCompetitorByIdAsString(competitorId);
                if (competitor != null) {
                    result.add(competitor);
                }
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(CompetitorNotificationPreference pref);
}
