package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplateResolver;

public class RaceLogOnLeaderboardIdentifier implements RaceLogIdentifierTemplate {

    private final String leaderboardName;
    
    public RaceLogOnLeaderboardIdentifier(FlexibleLeaderboard leaderboard) {
        this.leaderboardName = leaderboard.getName();
    }

    @Override
    public RaceLogIdentifier compile(RaceColumn column, Fleet fleet) {
        return new RaceLogIdentifierImpl(this, column, fleet);
    }

    @Override
    public void resolve(RaceLogIdentifierTemplateResolver resolver) {
        resolver.resolveOnLeaderboardIdentifier(this);
    }

    @Override
    public String getHostName() {
        return leaderboardName;
    }

}
