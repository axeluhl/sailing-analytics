package com.sap.sailing.domain.anniversary;

public class AnniversaryConflictResolver {

    public AnniversaryRaceInfo resolve(AnniversaryRaceInfo current, AnniversaryRaceInfo contender) {
        if(current.eventID.equals(contender.eventID) && current.leaderboardName.equals(contender.leaderboardName)){
            //same for relevant values, it does not matter
            return current;
        }
        if(current.getLeaderboardName().equals(contender.getIdentifier().getRaceName())){
            System.out.println("Resolved conflict " + current + " " + contender + " with current");
            return current;
        }
        System.out.println("Resolved conflict " + current + " " + contender + " with contender");
        return contender;
    }

}
