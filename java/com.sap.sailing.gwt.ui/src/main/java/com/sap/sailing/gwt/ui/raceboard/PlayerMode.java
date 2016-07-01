package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Duration;
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
    
    /**
     * Called after the {@link RaceTimePanel} has reacted to this update. We assume that now the timing for the race has been
     * received, and it should be clear by now whether we're talking about a live or a replay race. In case of a replay race
     * the timer is set to 
     */
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        if (getTimer().getPlayMode() != PlayModes.Live && !raceTimesInfo.isEmpty() && raceTimesInfo.containsKey(getRaceIdentifier())) {
            final RaceTimesInfoDTO times = raceTimesInfo.get(getRaceIdentifier());
            if (times.startOfRace != null) {
                getTimer().setTime(new MillisecondsTimePoint(times.startOfRace).minus(DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES).asMillis());
                getTimer().play();
                // we've done our adjustments; remove listener and let go
                super.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
            }
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // it's important to first unregister the listener before updateSettings is called because
        // updateSettings will trigger another leaderboard load, leading to an endless recursion otherwise
        super.updatedLeaderboard(leaderboard);
        final LeaderboardSettings existingSettings = getLeaderboardPanel().getSettings();
        final List<DetailType> raceDetailsToShow = new ArrayList<>(existingSettings.getRaceDetailsToShow());
        raceDetailsToShow.add(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        raceDetailsToShow.add(DetailType.RACE_GAP_TO_LEADER_IN_SECONDS);
        final LeaderboardSettings newSettings = new LeaderboardSettings(existingSettings.getManeuverDetailsToShow(),
                existingSettings.getLegDetailsToShow(),
                raceDetailsToShow, existingSettings.getOverallDetailsToShow(), existingSettings.getNamesOfRaceColumnsToShow(),
                existingSettings.getNamesOfRacesToShow(),
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
}
