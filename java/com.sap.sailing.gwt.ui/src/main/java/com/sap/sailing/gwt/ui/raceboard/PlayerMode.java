package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * Puts the race viewer into player mode, setting the timer to 10s before start and into {@link PlayStates#Playing} state if it's
 * not a live race where auto-play is started anyway.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PlayerMode extends AbstractRaceBoardMode {
    private final Duration DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES = Duration.ONE_SECOND.times(10);
    
    private boolean adjustedLeaderboardSettings;

    private boolean timerAdjusted;
    
    private void adjustLeaderboardSettings() {
        final LeaderboardSettings existingSettings = getLeaderboardPanel().getSettings();
        final List<DetailType> raceDetailsToShow = new ArrayList<>(existingSettings.getRaceDetailsToShow());
        raceDetailsToShow.add(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        raceDetailsToShow.add(DetailType.RACE_GAP_TO_LEADER_IN_SECONDS);
        final LeaderboardSettings newSettings = new LeaderboardSettings(
                Util.cloneListOrNull(existingSettings.getManeuverDetailsToShow()),
                Util.cloneListOrNull(existingSettings.getLegDetailsToShow()),
                raceDetailsToShow,
                Util.cloneListOrNull(existingSettings.getOverallDetailsToShow()),
                Util.cloneListOrNull(existingSettings.getNamesOfRaceColumnsToShow()),
                Util.cloneListOrNull(existingSettings.getNamesOfRacesToShow()),
                existingSettings.getNumberOfLastRacesToShow(), /* auto-expand pre-selected race */ true,
                existingSettings.getDelayBetweenAutoAdvancesInMilliseconds(),
                existingSettings.getNameOfRaceToSort(), existingSettings.isSortAscending(),
                existingSettings.isUpdateUponPlayStateChange(),
                existingSettings.getActiveRaceColumnSelectionStrategy(),
                existingSettings.isShowAddedScores(),
                existingSettings.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor(),
                existingSettings.isShowCompetitorSailIdColumn(),
                existingSettings.isShowCompetitorFullNameColumn());
        getLeaderboardPanel().updateSettings(newSettings);
    }
    
    @Override
    protected void trigger() {
        if (getTimer().getPlayMode() == PlayModes.Live) {
            stopReceivingRaceTimesInfos(); // this trigger wouldn't be stopped otherwise
        }
        if (!adjustedLeaderboardSettings && getLeaderboard() != null) {
            adjustedLeaderboardSettings = true;
            // it's important to first unregister the listener before updateSettings is called because
            // updateSettings will trigger another leaderboard load, leading to an endless recursion otherwise
            stopReceivingLeaderboard();
            adjustLeaderboardSettings();
        }
        if (!timerAdjusted && getTimer().getPlayMode() != PlayModes.Live && getRaceTimesInfoForRace() != null && getRaceTimesInfoForRace().startOfRace != null) {
            timerAdjusted = true;
            getTimer().setTime(new MillisecondsTimePoint(getRaceTimesInfoForRace().startOfRace).minus(DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES).asMillis());
            getTimer().play();
            // we've done our adjustments; remove listener and let go
            stopReceivingRaceTimesInfos();
        }
    }
}
