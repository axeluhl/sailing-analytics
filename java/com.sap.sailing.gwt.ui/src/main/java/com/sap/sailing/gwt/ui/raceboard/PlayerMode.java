package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * Puts the race viewer into player mode, setting the timer to 10s before start and into {@link PlayStates#Playing} state if it's
 * not a live race where auto-play is started anyway.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PlayerMode implements RaceBoardMode, RaceTimesInfoProviderListener, LeaderboardUpdateListener {
    private final Duration DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES = Duration.ONE_SECOND.times(10);
    private Timer timer;
    private RegattaAndRaceIdentifier raceIdentifier;
    private RaceTimePanel raceTimePanel;
    private LeaderboardPanel leaderboardPanel;
    
    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        this.raceTimePanel = raceBoardPanel.getRaceTimePanel();
        this.raceTimePanel.addRaceTimesInfoProviderListener(this);
        this.leaderboardPanel = raceBoardPanel.getLeaderboardPanel();
        this.leaderboardPanel.addLeaderboardUpdateListener(this);
        this.timer = raceBoardPanel.getTimer();
        this.raceIdentifier = raceBoardPanel.getSelectedRaceIdentifier();
    }

    /**
     * Called after the {@link RaceTimePanel} has reacted to this update. We assume that now the timing for the race has been
     * received, and it should be clear by now whether we're talkign about a live or a replay race. In case of a replay race
     * the timer is set to 
     */
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        if (timer.getPlayMode() != PlayModes.Live && !raceTimesInfo.isEmpty() && raceTimesInfo.containsKey(raceIdentifier)) {
            final RaceTimesInfoDTO times = raceTimesInfo.get(raceIdentifier);
            if (times.startOfRace != null) {
                timer.setTime(new MillisecondsTimePoint(times.startOfRace).minus(DURATION_BEFORE_START_TO_SET_TIMER_TO_FOR_REPLAY_RACES).asMillis());
                timer.play();
                // we've done our adjustments; remove listener and let go
                raceTimePanel.removeRaceTimesInfoProviderListener(this);
            }
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // it's important to first unregister the listener before updateSettings is called because
        // updateSettings will trigger another leaderboard load, leading to an endless recursion otherwise
        leaderboardPanel.removeLeaderboardUpdateListener(this);
        final LeaderboardSettings existingSettings = leaderboardPanel.getSettings();
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
        leaderboardPanel.updateSettings(newSettings);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        // nothing to do
    }
}
