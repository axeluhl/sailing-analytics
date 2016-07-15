package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.events.idle.IdleMapEvent;
import com.google.gwt.maps.client.events.idle.IdleMapHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.actions.GetLeaderboardByNameAction;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
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
public class StartAnalysisMode extends RaceBoardModeWithPerRaceCompetitors {
    private static final Duration DURATION_AFTER_START_TO_SET_TIMER_TO_FOR_START_ANALYSIS = Duration.ONE_SECOND.times(10);
    private static final Duration DURATION_BEFORE_START_TO_INCLUDE_IN_CHART_TIME_RANGE = Duration.ONE_SECOND.times(30);
    private static final Duration DURATION_AFTER_START_TO_INCLUDE_IN_CHART_TIME_RANGE = Duration.ONE_SECOND.times(30);
    private MultiCompetitorRaceChart competitorChart;
    private boolean raceTimesInfoReceived;
    private boolean leaderboardUpdateReveiced;
    private RaceTimesInfoDTO raceTimesInfo;

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
        if (!raceTimesInfo.isEmpty() && raceTimesInfo.containsKey(getRaceIdentifier())) {
            final RaceTimesInfoDTO times = raceTimesInfo.get(getRaceIdentifier());
            if (times.startOfRace != null) {
                // make sure we're no longer in live/playing mode/state; the start analysis is supposed to be a "frozen" display at first
                if (getTimer().getPlayMode() == PlayModes.Live) {
                    getTimer().setPlayMode(PlayModes.Replay);
                }
                // the following call will always trigger a leaderboard load and therefore a callback
                // to updatedLeaderboard; therefore, updatedLeaderboard can decide when it's time to
                // trigger the chart display and the map settings application
                getTimer().setTime(new MillisecondsTimePoint(times.startOfRace).plus(DURATION_AFTER_START_TO_SET_TIMER_TO_FOR_START_ANALYSIS).asMillis());
                // we've done our adjustments; remove listener and let go
                super.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
                this.raceTimesInfo = times;
                raceTimesInfoReceived = true;
            }
        }
    }
    
    @Override
    protected void updateCompetitorSelection() {
        updateCompetitorSelection(/* howManyTopCompetitorsInRaceToSelect */ 3);
    }

    private void showCompetitorChartIfAllDataReceived() {
        if (getRaceColumn() != null && leaderboardUpdateReveiced && raceTimesInfoReceived && getLeaderboard() != null
                && getLeaderboard().getCompetitorsFromBestToWorst(getRaceColumn()) != null
                && raceTimesInfo != null && raceTimesInfo.startOfRace != null) {
            ArrayList<String> raceColumnName = new ArrayList<>();
            raceColumnName.add(getRaceColumn().getName());
            // The problem with the leaderboard received in updatedLeaderboard(...) is that we don't know which request
            // has caused it to be delivered; there is an early request for "now" and maybe even another request for start of race;
            // therefore, we need to fire our own request for the time point desired and make sure we get the right
            // LeaderboardDTO to determine the competitor ordering for the current race.
            final GetLeaderboardByNameAction getLeaderboardByNameAction = new GetLeaderboardByNameAction(getLeaderboardPanel().getSailingService(), getLeaderboardPanel().getLeaderboard().name,
                    getTimer().getTime(), raceColumnName, /* addOverallDetails */ false,
                    getLeaderboard(), /* fillTotalPointsUncorrected */ false,
                    /* timerToAdjustOffsetIn */ getTimer(), /* errorReporter */ null, StringMessages.INSTANCE);
            getLeaderboardPanel().getExecutor().execute(getLeaderboardByNameAction, LeaderboardPanel.LOAD_LEADERBOARD_DATA_CATEGORY,
                            new AsyncCallback<LeaderboardDTO>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    GWT.log("Error trying to load leaderboard", caught);
                                }

                                @Override
                                public void onSuccess(LeaderboardDTO result) {
                                    setLeaderboard(result);
                                    getLeaderboardPanel().updateLeaderboard(result);
                                    updateCompetitorSelection();
                                    getRaceBoardPanel().setCompetitorChartVisible(true);
                                }
                            });
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {
                    // keep trying in 500ms steps until the simulation overlay is found; this way, changing the competitor chart
                    // settings is largely more successful because 
                    boolean result = getRaceBoardPanel().getMap().getSimulationOverlay() == null;
                    if (!result) {
                        final MultiCompetitorRaceChartSettings newCompetitorChartSettings = new MultiCompetitorRaceChartSettings(
                                new ChartSettings(/* stepSizeInMillis */ 1000), DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                                /* no second series */ null);
                        competitorChart.updateSettings(newCompetitorChartSettings);
                        getRaceTimePanel().getTimeRangeProvider().setTimeZoom(
                                new MillisecondsTimePoint(raceTimesInfo.startOfRace).minus(DURATION_BEFORE_START_TO_INCLUDE_IN_CHART_TIME_RANGE).asDate(),
                                new MillisecondsTimePoint(raceTimesInfo.startOfRace).plus(DURATION_AFTER_START_TO_INCLUDE_IN_CHART_TIME_RANGE).asDate());
                        final RaceMapSettings existingMapSettings = getRaceBoardPanel().getMap().getSettings();
                        final RaceMapSettings newMapSettings = new RaceMapSettings(
                                new RaceMapZoomSettings(Collections.singleton(ZoomTypes.BOATS), /* zoomToSelected */ false),
                                existingMapSettings.getHelpLinesSettings(),
                                existingMapSettings.getTransparentHoverlines(),
                                existingMapSettings.getHoverlineStrokeWeight(),
                                existingMapSettings.getTailLengthInMilliseconds(),
                                /* existingMapSettings.isWindUp() */ true,
                                existingMapSettings.getBuoyZoneRadiusInMeters(),
                                existingMapSettings.isShowOnlySelectedCompetitors(),
                                existingMapSettings.isShowSelectedCompetitorsInfo(),
                                existingMapSettings.isShowWindStreamletColors(),
                                existingMapSettings.isShowWindStreamletOverlay(),
                                existingMapSettings.isShowSimulationOverlay(),
                                existingMapSettings.isShowMapControls(),
                                existingMapSettings.getManeuverTypesToShow(),
                                existingMapSettings.isShowDouglasPeuckerPoints());
                        // try to update the settings once; the problem is the "wind up" display; it changes pan/zoom
                        // which is a major source of instability for the map. Going twice in the map idle event handler
                        // to ensure the settings are really applied.
                        final Object[] registration = new Object[1];
                        registration[0] = getRaceBoardPanel().getMap().getMap().addIdleHandler(new IdleMapHandler() {
                            @Override
                            public void onEvent(IdleMapEvent event) {
                                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                                    @Override
                                    public void execute() {
                                        getRaceBoardPanel().getMap().updateSettings(newMapSettings);
                                    }
                                });
                                ((HandlerRegistration) registration[0]).removeHandler();
                            }
                        });
                        getRaceBoardPanel().getMap().updateSettings(newMapSettings);
                    }
                    return result;
                }
            }, /* delay in milliseconds */ 500);
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        // no need to do anything if we don't have the timing info yet; after obtaining the timing
        // info there will always be at least one more leaderboard requested, triggering a callback
        // to this method
        if (raceTimesInfoReceived) {
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
            leaderboardUpdateReveiced = true;
            showCompetitorChartIfAllDataReceived();
        }
    }

}
