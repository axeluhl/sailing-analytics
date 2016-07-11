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
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.MarkPassingTimesDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * Puts the race viewer in a mode where the user can see what may be called the "Winning Lanes." For this,
 * the timer is set to the point in time when the first competitor finishes the race, or, for live races,
 * to the current point in time. The tail length is chosen such that it covers the full track of the
 * competitor farthest ahead.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WinningLanesMode extends AbstractRaceBoardMode {
    private RaceColumnDTO raceColumn;
    
    private Duration tailLength;
    
    /**
     * When set to {@code true}, when {@link #updatedLeaderboard(LeaderboardDTO)} is called the next time it will
     * stop the leaderboard update notifications and will adjust the tail length to {@link #tailLength}. Must only
     * be set to {@code true} if {@link #tailLength} has been set to a valid duration before.
     */
    private boolean stopReceivingLeaderboardUpdatesAndAdjustTailLength;

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        final Date startOfRace;
        final RaceTimesInfoDTO raceTimesInfoForRace;
        if (!raceTimesInfo.isEmpty() && raceTimesInfo.containsKey(getRaceIdentifier()) &&
                (startOfRace=(raceTimesInfoForRace=raceTimesInfo.get(getRaceIdentifier())).startOfRace) != null) {
            if (getTimer().getPlayMode() == PlayModes.Live) {
                // adjust tail length such that for the leading boat the tail is shown since the start time point
                tailLength = new MillisecondsTimePoint(startOfRace).until(getTimer().getLiveTimePoint());
                stopReceivingLeaderboardUpdatesAndAdjustTailLength = true;
            } else {
                final List<MarkPassingTimesDTO> markPassingTimes = raceTimesInfoForRace.getMarkPassingTimes();
                final Date firstPassingOfLastWaypointPassed = markPassingTimes == null || markPassingTimes.isEmpty() ? null :
                    markPassingTimes.get(markPassingTimes.size()-1).firstPassingDate;
                final Date endOfRace = raceTimesInfoForRace.endOfRace;
                final Date end = firstPassingOfLastWaypointPassed != null ? firstPassingOfLastWaypointPassed :
                    endOfRace != null ? endOfRace : raceTimesInfoForRace.endOfTracking;
                if (end != null) {
                    tailLength = new MillisecondsTimePoint(startOfRace).until(new MillisecondsTimePoint(end));
                    stopReceivingLeaderboardUpdatesAndAdjustTailLength = true;
                    getTimer().setTime(end.getTime());
                }
                // we've done our adjustments; remove listener and let go
                super.raceTimesInfosReceived(raceTimesInfo, clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
            }
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        if (stopReceivingLeaderboardUpdatesAndAdjustTailLength) {
            // it's important to first unregister the listener before updateSettings is called because
            // updateSettings will trigger another leaderboard load, leading to an endless recursion otherwise
            super.updatedLeaderboard(leaderboard);
            final LeaderboardSettings existingSettings = getLeaderboardPanel().getSettings();
            final List<DetailType> raceDetailsToShow = new ArrayList<>(existingSettings.getRaceDetailsToShow());
            raceDetailsToShow.add(DetailType.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS);
            raceDetailsToShow.add(DetailType.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS);
            raceDetailsToShow.add(DetailType.RACE_DISTANCE_TRAVELED);
            raceDetailsToShow.add(DetailType.RACE_TIME_TRAVELED);
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
            final int howManyCompetitorsToSelect = getHowManyCompetitorsToSelect(getLeaderboardPanel().getCompetitors(getRaceIdentifier()));
            final Iterable<CompetitorDTO> topCompetitorsInRaceToSelect = leaderboard.getCompetitorsFromBestToWorst(raceColumn.getName()).subList(0, howManyCompetitorsToSelect);
            getRaceBoardPanel().getCompetitorSelectionProvider().setSelection(topCompetitorsInRaceToSelect);
            final RaceMapSettings existingMapSettings = getRaceBoardPanel().getMap().getSettings();
            final RaceMapSettings newMapSettings = new RaceMapSettings(existingMapSettings.getZoomSettings(),
                    existingMapSettings.getHelpLinesSettings(),
                    existingMapSettings.getTransparentHoverlines(),
                    existingMapSettings.getHoverlineStrokeWeight(),
                    tailLength.asMillis(),
                    existingMapSettings.isWindUp(),
                    existingMapSettings.getBuoyZoneRadiusInMeters(),
                    /* existingMapSettings.isShowOnlySelectedCompetitors() */ true, // show the top n competitors and their tails quickly
                    existingMapSettings.isShowSelectedCompetitorsInfo(),
                    existingMapSettings.isShowWindStreamletColors(),
                    existingMapSettings.isShowWindStreamletOverlay(),
                    existingMapSettings.isShowSimulationOverlay(),
                    existingMapSettings.isShowMapControls(),
                    existingMapSettings.getManeuverTypesToShow(),
                    existingMapSettings.isShowDouglasPeuckerPoints());
            getRaceBoardPanel().getMap().updateSettings(newMapSettings);
        }
    }

    /**
     * Based on the set of competitors in the leaderboard, determines how many competitors to select for the "winning lanes"
     * view. The number is determined to be at least one tenth of the number of competitors, but at least one if there are
     * one or more competitors.
     */
    private int getHowManyCompetitorsToSelect(Iterable<CompetitorDTO> competitors) {
        return Util.isEmpty(competitors) ? 0 : Math.max(Util.size(competitors)/10, 1);
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
        this.raceColumn = raceColumn;
    }
}
