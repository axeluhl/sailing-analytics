package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplateResolver;

public class RaceLogOnLeaderboardIdentifier implements RaceLogIdentifierTemplate {
    private static final long serialVersionUID = -3117585697963420247L;
    
    private final String leaderboardName;
    private final String raceColumnName;
    
    public RaceLogOnLeaderboardIdentifier(FlexibleLeaderboard leaderboard, String raceColumnName) {
        this.leaderboardName = leaderboard.getName();
        this.raceColumnName = raceColumnName;
    }

    @Override
    public RaceLogIdentifier compile(Fleet fleet) {
        return new RaceLogIdentifierImpl(this, raceColumnName, fleet);
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
