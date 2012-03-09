package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * @param nameOfRaceToSort
     *            if <code>null</code>, don't sort any race column
     * @param raceToShow
     *            if <code>null</code>, the settings returned will cause the list of race columns shown to remain
     *            unchanged during {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}; otherwise, the settings
     *            will show the single race identified by this argument
     */
    public LeaderboardSettings createNewSettingsForPlayMode(PlayModes playMode, String nameOfRaceToSort, RaceInLeaderboardDTO raceToShow) {
        LeaderboardSettings settings = null;
        List<RaceInLeaderboardDTO> racesToShow = raceToShow == null ? null : Collections.singletonList(raceToShow);
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
                        racesToShow, /* autoExpandFirstRace */ true,
                        /* refresh interval */ null, /* delay to live */ null,
                        /* name of race to sort*/ nameOfRaceToSort, /* ascending */ true);
                break;
            case Replay:
                settings = createNewDefaultSettings(racesToShow, /* autoExpandFirstRace */ true);
                break;
        }
        return settings;
    }

    /**
     * @param racesToShow
     *            if <code>null</code>, create settings which leave the list of races to show unchanged when applied
     *            using {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}; otherwise, the list of races
     *            identified by this parameter will be shown, so that, e.g., an empty list causes no race columns to be
     *            shown
     */
    public LeaderboardSettings createNewDefaultSettings(List<RaceInLeaderboardDTO> racesToShow, boolean autoExpandFirstRace) {
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
        return new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, racesToShow, autoExpandFirstRace,
                /* refresh interval */ null, /* delay to live */ null,
                /* sort by column */ null, /* ascending */ true);
    }
}
