package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sse.IsManagedByCache;

public class DefaultFleetImpl extends FleetImpl implements IsManagedByCache<SharedDomainFactory> {

    private static final long serialVersionUID = 6233489616955255401L;

    public DefaultFleetImpl() {
        super(LeaderboardNameConstants.DEFAULT_FLEET_NAME);
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        return FlexibleLeaderboardImpl.defaultFleet;
    }

}
