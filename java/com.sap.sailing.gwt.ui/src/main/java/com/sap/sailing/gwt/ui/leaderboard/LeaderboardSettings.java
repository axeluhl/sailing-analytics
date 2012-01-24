package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.domain.common.DetailType;

public class LeaderboardSettings {
    private final List<RaceInLeaderboardDTO> raceColumnsToShow;
    private final List<DetailType> maneuverDetailsToShow;
    private final List<DetailType> legDetailsToShow;
    private final List<DetailType> raceDetailsToShow;
    private final long delayBetweenAutoAdvancesInMilliseconds;
    private final long delayInMilliseconds;
    
    public LeaderboardSettings(List<DetailType> meneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<RaceInLeaderboardDTO> raceColumnsToShow,
            long delayBetweenAutoAdvancesInMilliseconds, long delayInMilliseconds) {
        this.legDetailsToShow = legDetailsToShow;
        this.raceDetailsToShow = raceDetailsToShow;
        this.raceColumnsToShow = raceColumnsToShow;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        this.delayInMilliseconds = delayInMilliseconds;
        this.maneuverDetailsToShow = meneuverDetailsToShow;
    }
  
    public List<DetailType> getManeuverDetailsToShow() {
        return maneuverDetailsToShow;
    }

    public List<DetailType> getLegDetailsToShow() {
        return legDetailsToShow;
    }

    public List<DetailType> getRaceDetailsToShow() {
        return raceDetailsToShow;
    }
    
    public List<RaceInLeaderboardDTO> getRaceColumnsToShow(){
        return raceColumnsToShow;
    }

    public long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds;
    }

    public long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }
}
