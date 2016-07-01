package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

/**
 * The start analysis mode makes the competitor chart visible and sets it to speed over ground; the
 * {@link LeaderboardSettings} are adjusted such that no leg columns but only start parameters are
 * shown. The top three starters are selected when the leaderboard has been updated after setting
 * the timer to a few seconds after the start. The {@link PlayStates#Paused} is used for the timer.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StartAnalysisMode extends AbstractRaceBoardMode {
    private static final Duration DURATION_AFTER_START_TO_SET_TIMER_TO_FOR_START_ANALYSIS = Duration.ONE_SECOND.times(10);
    private static final Duration DURATION_BEFORE_START_TO_INCLUDE_IN_CHART_TIME_RANGE = Duration.ONE_SECOND.times(20);
    private static final Duration DURATION_AFTER_START_TO_INCLUDE_IN_CHART_TIME_RANGE = Duration.ONE_SECOND.times(20);
    private MultiCompetitorRaceChart competitorChart;
    private boolean raceTimesInfoReceived;
    private boolean leaderboardUpdateReveiced;
    private LeaderboardDTO leaderboard;
    private RaceTimesInfoDTO raceTimesInfo;
    private RaceColumnDTO raceColumn;

    @Override
    public void applyTo(RaceBoardPanel raceBoardPanel) {
        super.applyTo(raceBoardPanel);
        this.competitorChart = raceBoardPanel.getCompetitorChart();
    }

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
                getTimer().setTime(new MillisecondsTimePoint(times.startOfRace).plus(DURATION_AFTER_START_TO_SET_TIMER_TO_FOR_START_ANALYSIS).asMillis());
                // we've done our adjustments; remove listener and let go
                super.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
                this.raceTimesInfo = times;
                raceTimesInfoReceived = true;
                showCompetitorChartIfAllDataReceived();
            }
        }
    }

    private void showCompetitorChartIfAllDataReceived() {
        if (raceColumn != null && leaderboardUpdateReveiced && raceTimesInfoReceived && leaderboard != null
                && leaderboard.getCompetitorsFromBestToWorst(raceColumn) != null
                && raceTimesInfo != null && raceTimesInfo.startOfRace != null) {
            final Set<CompetitorDTO> competitorsToSelect = new HashSet<>();
            for (int i=0; i<3 && i<leaderboard.getCompetitorsFromBestToWorst(raceColumn).size(); i++) {
                competitorsToSelect.add(leaderboard.getCompetitorsFromBestToWorst(raceColumn).get(i));
            }
            getRaceBoardPanel().setCompetitorChartVisible(true);
            getRaceBoardPanel().getCompetitorSelectionProvider().setSelection(competitorsToSelect);
            MultiCompetitorRaceChartSettings newCompetitorChartSettings = new MultiCompetitorRaceChartSettings(
                    new ChartSettings(/* stepSizeInMillis */ 1000), DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
            competitorChart.updateSettings(newCompetitorChartSettings);
            getRaceTimePanel().getTimeRangeProvider().setTimeZoom(
                    new MillisecondsTimePoint(raceTimesInfo.startOfRace).minus(DURATION_BEFORE_START_TO_INCLUDE_IN_CHART_TIME_RANGE).asDate(),
                    new MillisecondsTimePoint(raceTimesInfo.startOfRace).plus(DURATION_AFTER_START_TO_INCLUDE_IN_CHART_TIME_RANGE).asDate());
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // it's important to first unregister the listener before updateSettings is called because
        // updateSettings will trigger another leaderboard load, leading to an endless recursion otherwise
        super.updatedLeaderboard(leaderboard);
        final LeaderboardSettings existingSettings = getLeaderboardPanel().getSettings();
        final List<DetailType> raceDetailsToShow = new ArrayList<>(existingSettings.getRaceDetailsToShow());
        raceDetailsToShow.add(DetailType.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START);
        raceDetailsToShow.add(DetailType.RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_RACE_START);
        raceDetailsToShow.add(DetailType.DISTANCE_TO_START_AT_RACE_START);
        raceDetailsToShow.add(DetailType.DISTANCE_TO_START_LINE);
        raceDetailsToShow.add(DetailType.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_PASSING_START_IN_METERS);
        raceDetailsToShow.add(DetailType.SPEED_OVER_GROUND_AT_RACE_START);
        raceDetailsToShow.add(DetailType.SPEED_OVER_GROUND_WHEN_PASSING_START);
        raceDetailsToShow.add(DetailType.START_TACK);
        raceDetailsToShow.remove(DetailType.DISPLAY_LEGS);
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
        this.leaderboard = leaderboard;
        leaderboardUpdateReveiced = true;
        showCompetitorChartIfAllDataReceived();
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        this.raceColumn = raceColumn;
    }
}
