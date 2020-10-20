package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.CompetitorNotificationPreferences;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.interfaces.UserStore;

/**
 * {@link PreferenceObjectBasedNotificationSet} for associations of {@link Competitor} to a set of users to notify about
 * specific events for the {@link Competitor}. Subclasses define the concrete case of notification based on the flags in
 * {@link CompetitorNotificationPreference} via implementing {@link #shouldNotifyFor(CompetitorNotificationPreference)}.
 */
public abstract class AbstractCompetitorNotificationSet
        extends PreferenceObjectBasedNotificationSet<CompetitorNotificationPreferences, Competitor> {
    private ServiceTracker<RacingEventService, RacingEventService> tracker;
    //TODO: Evaluate if this TimeOut is appropriate.
    private final long TIMEOUT = 2500;

    public AbstractCompetitorNotificationSet(UserStore userStore) {
        super(CompetitorNotificationPreferences.PREF_NAME, userStore);
    }

    /**
     * Constructor used to automatically track {@link UserStore} and {@link CompetitorAndBoatStore} as OSGi service.
     */
    public AbstractCompetitorNotificationSet(BundleContext bundleContext) {
        super(CompetitorNotificationPreferences.PREF_NAME, bundleContext);
        if (bundleContext == null) {
            this.tracker = null;
        } else {
            this.tracker = new ServiceTracker<RacingEventService, RacingEventService>(bundleContext,
                    RacingEventService.class, null);
            this.tracker.open();
        }
    }
    
    private CompetitorAndBoatStore getStore() {
        CompetitorAndBoatStore competitorAndBoatStoreOrNull = null; 
        try {
            RacingEventService service = tracker.waitForService(TIMEOUT);
            competitorAndBoatStoreOrNull = service.getCompetitorAndBoatStore();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return competitorAndBoatStoreOrNull;
    }

    @Override
    protected Collection<Competitor> calculateObjectsToNotify(CompetitorNotificationPreferences preference) {
        Set<Competitor> result = new HashSet<>();
        for (CompetitorNotificationPreference pref : preference.getCompetitors()) {
            if (pref.isNotifyAboutResults()) {
                String competitorId = pref.getCompetitorId();
                    CompetitorAndBoatStore store = getStore();
                    if(store != null) {
                        DynamicCompetitor competitor = store.getExistingCompetitorByIdAsString(competitorId);
                        if (competitor != null) {
                            result.add(competitor);
                        }
                    }
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(CompetitorNotificationPreference pref);
}
