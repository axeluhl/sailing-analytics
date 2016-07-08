package com.sap.sailing.server.impl.preferences;

import com.sap.sailing.domain.common.preferences.BoatClassNotificationPreference;
import com.sap.sse.security.UserStore;

public class BoatClassResultsNotificationSet extends AbstractBoatClassNotificationSet {

    public BoatClassResultsNotificationSet(UserStore store) {
        super(store);
    }

    @Override
    protected boolean shouldNotifyFor(BoatClassNotificationPreference pref) {
        return pref.isNotifyAboutResults();
    }
}
