package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;

/*
 * A factory class creating leaderboard settings for different contexts (user role, live or replay mode, etc.
 */
public class LeaderboardSettingsFactory {

    public static LeaderboardSettings getSettingsForPlayMode(PlayModes playMode) {
        LeaderboardSettings settings = null;
        switch (playMode) {
            case Live:  
                ArrayList<DetailType> maneuverDetails = new ArrayList<DetailType>();
                maneuverDetails.add(DetailType.TACK);
                maneuverDetails.add(DetailType.JIBE);
                maneuverDetails.add(DetailType.PENALTY_CIRCLE);

                ArrayList<DetailType> legDetails = new ArrayList<DetailType>();
                legDetails.add(DetailType.DISTANCE_TRAVELED);
                legDetails.add(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
                legDetails.add(DetailType.RANK_GAIN);
                
                ArrayList<DetailType> raceDetails = new ArrayList<DetailType>();

                ArrayList<RaceInLeaderboardDTO> raceColumns = new ArrayList<RaceInLeaderboardDTO>();
                
                settings = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, raceColumns, true, 0l, 0l);
                break;
            case Replay:
                settings = getDefaultSettings();
                break;
        }
        return settings;
    }

    public static LeaderboardSettings getDefaultSettings() {
        ArrayList<DetailType> maneuverDetails = new ArrayList<DetailType>();
        maneuverDetails.add(DetailType.TACK);
        maneuverDetails.add(DetailType.JIBE);
        maneuverDetails.add(DetailType.PENALTY_CIRCLE);

        ArrayList<DetailType> legDetails = new ArrayList<DetailType>();
        legDetails.add(DetailType.DISTANCE_TRAVELED);
        legDetails.add(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        legDetails.add(DetailType.RANK_GAIN);
        
        ArrayList<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.DISPLAY_LEGS);

        ArrayList<RaceInLeaderboardDTO> raceColumns = new ArrayList<RaceInLeaderboardDTO>();
        
        return new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, raceColumns, true, 0l, 0l);
    }
}
