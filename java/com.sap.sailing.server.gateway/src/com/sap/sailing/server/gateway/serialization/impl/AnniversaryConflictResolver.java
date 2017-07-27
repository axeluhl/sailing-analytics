package com.sap.sailing.server.gateway.serialization.impl;

public class AnniversaryConflictResolver {

    public AnniversaryRaceInfo resolve(AnniversaryRaceInfo current, AnniversaryRaceInfo contender) {
        if(current.getLeaderboardName().equals(contender.getIdentifier().getRaceName())){
            return current;
        }
        return contender;
    }

}
