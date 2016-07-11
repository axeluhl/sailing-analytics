package com.sap.sailing.server.impl.preferences;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreferences;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.UserStore;

public abstract class AbstractBoatClassNotificationSet
        extends PreferenceObjectBasedNotificationSet<BoatClassNotificationPreferences, BoatClass> {

    public AbstractBoatClassNotificationSet(UserStore store) {
        super(BoatClassNotificationPreferences.PREF_NAME, store);
    }

    @Override
    protected Collection<BoatClass> calculateObjectsToNotify(BoatClassNotificationPreferences preference) {
        Set<BoatClass> result = new HashSet<>();
        for (BoatClassNotificationPreference pref : preference.getBoatClasses()) {
            if (shouldNotifyFor(pref)) {
                result.add(pref.getBoatClass());
            }
        }
        return result;
    }

    protected abstract boolean shouldNotifyFor(BoatClassNotificationPreference pref);

}
