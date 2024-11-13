package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.data.SailorProfiles;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.server.preferences.SailorProfilePreference;
import com.sap.sailing.server.preferences.SailorProfilePreferences;
import com.sap.sse.security.SecurityService;

public class LeaderboardGroupWithContext implements HasLeaderboardGroupContext {
    private final LeaderboardGroup leaderboardGroup;
    private final PolarDataService polarDataService;
    private final DomainFactory baseDomainFactory;
    private final SecurityService securityService;
    
    /**
     * Access has to be synchronized; it is constructed lazily by the {@link #getSailorProfiles()} method.
     */
    private SailorProfiles sailorProfiles;

    public LeaderboardGroupWithContext(LeaderboardGroup leaderboardGroup, PolarDataService polarDataService, DomainFactory baseDomainFactory, SecurityService securityService) {
        this.leaderboardGroup = leaderboardGroup;
        this.polarDataService = polarDataService;
        this.baseDomainFactory = baseDomainFactory;
        this.securityService = securityService;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }

    @Override
    public DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }
    
    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }
    
    @Override
    public synchronized SailorProfiles getSailorProfiles() {
        if (sailorProfiles == null) {
            final SailorProfilePreferences sailorProfilePreferences = getSecurityService().getPreferenceObject(getSecurityService().getCurrentUser().getName(), SailorProfilePreferences.PREF_NAME);
            for (final SailorProfilePreference sailorProfilePreference : sailorProfilePreferences.getSailorProfiles()) {
                
            }
        }
        return sailorProfiles;
    }
}
