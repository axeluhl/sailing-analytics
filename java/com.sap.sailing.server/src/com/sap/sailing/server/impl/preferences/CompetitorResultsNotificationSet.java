package com.sap.sailing.server.impl.preferences;

import com.sap.sailing.domain.common.preferences.CompetitorNotificationPreference;
import com.sap.sse.security.UserStore;

public class CompetitorResultsNotificationSet extends AbstractCompetitorNotificationSet {

    public CompetitorResultsNotificationSet(UserStore store) {
        super(store);
    }

    @Override
    protected boolean shouldNotifyFor(CompetitorNotificationPreference pref) {
        return pref.isNotifyAboutResults();
    }

}
