package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;

/*
 * A factory class creating leaderboard settings for different contexts (user role, live or replay mode, etc.
 */
public class LeaderboardSettingsFactory {
    private static LeaderboardSettingsFactory instance;
    
    public synchronized static LeaderboardSettingsFactory getInstance() {
        if (instance == null) {
            instance = new LeaderboardSettingsFactory();
        }
        return instance;
    }

    /**
     * @param nameOfRaceToSort if <code>null</code>, don't sort any race column
     */
    public LeaderboardSettings createNewSettingsForPlayMode(PlayModes playMode, String nameOfRaceToSort) {
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
                raceDetails.add(DetailType.RACE_DISTANCE_TO_LEADER_IN_METERS);
                raceDetails.add(DetailType.NUMBER_OF_MANEUVERS);
                settings = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails,
                        /* don't change raceColumns */ null, true,
                        /* refresh interval */ null, /* delay to live */ null,
                        /* name of race to sort*/ nameOfRaceToSort, /* ascending */ true);
                break;
            case Replay:
                settings = createNewDefaultSettings(true);
                break;
        }
        return settings;
    }

    public LeaderboardSettings createNewDefaultSettings(boolean autoExpandFirstRace) {
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
        return new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, raceColumns, autoExpandFirstRace,
                /* refresh interval */ null, /* delay to live */ null,
                /* sort by column */ null, /* ascending */ true);
    }
}
