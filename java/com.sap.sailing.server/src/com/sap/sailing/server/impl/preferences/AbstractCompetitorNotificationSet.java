package com.sap.sailing.server.impl.preferences;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.preferences.CompetitorNotificationPreference;
import com.sap.sailing.domain.common.preferences.NotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.UserStore;

public abstract class AbstractCompetitorNotificationSet
        extends PreferenceObjectBasedNotificationSet<NotificationPreferences, Serializable> {

    public AbstractCompetitorNotificationSet(UserStore store) {
        super(NotificationPreferences.PREF_NAME, store);
    }

    @Override
    protected Collection<Serializable> calculateObjectsToNotify(NotificationPreferences preference) {
        Set<Serializable> result = new HashSet<>();
        for (CompetitorNotificationPreference pref : preference.getCompetitorPreferences().getCompetitors()) {
            if (shouldNotifyFor(pref)) {
                result.add(pref.getCompetitorId());
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(CompetitorNotificationPreference pref);

}
