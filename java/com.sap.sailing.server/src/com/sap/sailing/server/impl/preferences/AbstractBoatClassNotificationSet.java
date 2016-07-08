package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.preferences.BoatClassNotificationPreference;
import com.sap.sailing.domain.common.preferences.NotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.UserStore;

public abstract class AbstractBoatClassNotificationSet
        extends PreferenceObjectBasedNotificationSet<NotificationPreferences, BoatClassMasterdata> {

    public AbstractBoatClassNotificationSet(UserStore store) {
        super(NotificationPreferences.PREF_NAME, store);
    }

    @Override
    protected Collection<BoatClassMasterdata> calculateObjectsToNotify(NotificationPreferences preference) {
        Set<BoatClassMasterdata> result = new HashSet<>();
        for (BoatClassNotificationPreference pref : preference.getBoatClassPreferences().getBoatClasses()) {
            if (shouldNotifyFor(pref)) {
                result.add(pref.getBoatClass());
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(BoatClassNotificationPreference pref);

}
