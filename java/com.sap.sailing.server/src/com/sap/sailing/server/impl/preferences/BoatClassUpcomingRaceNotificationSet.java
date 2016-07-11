package com.sap.sailing.server.impl.preferences;

import com.sap.sailing.server.impl.preferences.model.BoatClassNotificationPreference;
import com.sap.sse.security.UserStore;

public class BoatClassUpcomingRaceNotificationSet extends AbstractBoatClassNotificationSet {

    public BoatClassUpcomingRaceNotificationSet(UserStore store) {
        super(store);
    }

    @Override
    protected boolean shouldNotifyFor(BoatClassNotificationPreference pref) {
        return pref.isNotifyAboutUpcomingRaces();
    }
}
