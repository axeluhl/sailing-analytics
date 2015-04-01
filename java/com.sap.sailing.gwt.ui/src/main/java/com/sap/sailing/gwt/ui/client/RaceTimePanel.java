package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.MarkPassingTimesDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class RaceTimePanel extends TimePanel<RaceTimePanelSettings> implements RaceSelectionChangeListener, RaceTimesInfoProviderListener {
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    
    private RegattaAndRaceIdentifier selectedRace;
    private boolean autoAdjustPlayMode;
    private RaceTimesInfoDTO lastRaceTimesInfo;
    
    public RaceTimePanel(Timer timer, TimeRangeWithZoomProvider timeRangeProvider, StringMessages stringMessages,
            RaceTimesInfoProvider raceTimesInfoProvider, boolean canReplayWhileLiveIsPossible) {
        super(timer, timeRangeProvider, stringMessages, canReplayWhileLiveIsPossible);
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        selectedRace = null;
        autoAdjustPlayMode = true;
    }
    
    /**
     * If the race's start time is already known and the <code>time</code> is before the start we can set the count-down label
     */
    @Override
    protected String getTimeToStartLabelText(Date time) {
        String result = null;
        RaceTimesInfoDTO selectedRaceTimes = raceTimesInfoProvider.getRaceTimesInfo(selectedRace);
        if (selectedRaceTimes.startOfRace != null) {
            if(time.before(selectedRaceTimes.startOfRace) || time.equals(selectedRaceTimes.startOfRace)) {
                long timeToStartInMs = selectedRaceTimes.startOfRace.getTime() - time.getTime();
                result = timeToStartInMs < 1000 ? stringMessages.start() : stringMessages.timeToStart(DateAndTimeFormatterUtil.formatElapsedTime(timeToStartInMs));
            } else {
                long timeSinceStartInMs = time.getTime() - selectedRaceTimes.startOfRace.getTime();
                result = stringMessages.timeSinceStart(DateAndTimeFormatterUtil.formatElapsedTime(timeSinceStartInMs));
            } 
        }
        return result;
    }

    @Override
    public void updateSettings(RaceTimePanelSettings newSettings) {
        super.updateSettings(newSettings);
        raceTimesInfoProvider.setRequestInterval(newSettings.getRefreshInterval());
    }

    @Override
    public RaceTimePanelSettings getSettings() {
        RaceTimePanelSettings result = new RaceTimePanelSettings();
        result.setRefreshInterval(timer.getRefreshInterval());
        result.setRaceTimesInfo(raceTimesInfoProvider.getRaceTimesInfo(selectedRace));
        return result;
    }

    @Override
    public SettingsDialogComponent<RaceTimePanelSettings> getSettingsDialogComponent() {
        return new RaceTimePanelSettingsDialogComponent(getSettings(), stringMessages);
    }

    private void updateTimeInfo(RaceTimesInfoDTO raceTimesInfo) {
        if (raceTimesInfo == null) { 
            // in case the race is not tracked anymore we reset the time slider
            resetTimeSlider();
        } else {
            // timer will only accept this update if the delay hasn't been updated explicitly
            timer.setLivePlayDelayInMillis(raceTimesInfo.delayToLiveInMs);
            if ((raceTimesInfo.startOfTracking != null || raceTimesInfo.startOfRace != null) && 
                    (raceTimesInfo.newestTrackingEvent != null || raceTimesInfo.endOfRace != null)) {
                // we set here the min and max of the time slider, the start and end of the race as well as the known
                // leg markers
                boolean liveModeToBeMadePossible = isLiveModeToBeMadePossible();
                setLiveGenerallyPossible(liveModeToBeMadePossible);
                setJumpToLiveEnablement(liveModeToBeMadePossible && timer.getPlayMode() != PlayModes.Live);
                if (autoAdjustPlayMode && liveModeToBeMadePossible) {
                    timer.setPlayMode(PlayModes.Live);
                }
                updatePlayPauseButtonsVisibility(timer.getPlayMode());
                if (liveModeToBeMadePossible && !canReplayWhileLiveIsPossible() &&
                        timer.getPlayMode()==PlayModes.Replay && timer.getPlayState()==PlayStates.Playing) {
                    // pause timer because it must not be playing in replay mode while the race is live
                    timer.pause();
                }
                
                boolean timerAlreadyInitialized = getFromTime() != null && getToTime() != null && timeSlider.getCurrentValue() != null;
                if (!timeRangeProvider.isZoomed()) {
                    updateMinMax(raceTimesInfo);
                    if (!timerAlreadyInitialized) {
                        initTimerPosition(raceTimesInfo);
                    }
                    updateLegMarkers(raceTimesInfo);
                }
            } else {
                // the tracked race did not start yet or has no events yet
                // maybe show a special state for this like "Race did not start yet"
            }
        }
        lastRaceTimesInfo = raceTimesInfo;
    } 
    
    @Override
    public void onTimeZoomChanged(Date zoomStartTimepoint, Date zoomEndTimepoint) {
        super.onTimeZoomChanged(zoomStartTimepoint, zoomEndTimepoint);
        timeSlider.setZoomed(true);
        setMinMax(zoomStartTimepoint, zoomEndTimepoint, false);
        timeSlider.clearMarkersAndLabelsAndTicks();
        redrawAllMarkers(lastRaceTimesInfo);
    }

    @Override
    public void onTimeZoomReset() {
        super.onTimeZoomReset();
        timeSlider.setZoomed(false);
        timeSlider.setMinValue(new Double(timeRangeProvider.getFromTime().getTime()), false);
        timeSlider.setMaxValue(new Double(timeRangeProvider.getToTime().getTime()), false);
        timeSlider.setCurrentValue(new Double(timer.getTime().getTime()), true);
        timeSlider.clearMarkersAndLabelsAndTicks();
        redrawAllMarkers(lastRaceTimesInfo);
    }

    @Override
    protected boolean isLiveModeToBeMadePossible() {
        long eventTimeoutTolerance = 60 * 1000; // 60s
        long timeBeforeRaceStartTolerance = 3 * 60 * 1000; // 3min
        long liveTimePointInMillis = timer.getLiveTimePointInMillis();
        RaceTimesInfoDTO lastRaceTimesInfo = raceTimesInfoProvider != null ? raceTimesInfoProvider.getRaceTimesInfo(selectedRace) : null;
        return lastRaceTimesInfo != null &&
                lastRaceTimesInfo.newestTrackingEvent != null &&
                liveTimePointInMillis < lastRaceTimesInfo.newestTrackingEvent.getTime() + eventTimeoutTolerance &&
                ((lastRaceTimesInfo.startOfTracking != null && liveTimePointInMillis > lastRaceTimesInfo.startOfTracking.getTime())||
                 (lastRaceTimesInfo.startOfRace != null && liveTimePointInMillis > lastRaceTimesInfo.startOfRace.getTime() - timeBeforeRaceStartTolerance));
    }
    
    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            selectedRace = selectedRaces.iterator().next();
            if (!raceTimesInfoProvider.containsRaceIdentifier(selectedRace)) {
                raceTimesInfoProvider.addRaceIdentifier(selectedRace, true);
            }
        }
    }
    
    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        super.playStateChanged(playState, playMode);
        switch (playMode) {
        case Replay:
            autoAdjustPlayMode = false;
            break;
        case Live:
            break;
        }
    }

    /**
     * Obtains the min/max range for the slider bar from <code>newRaceTimesInfo</code> and adjusts the slider bar if
     * needed. Since we extend the slider bar to the right when playing across the "end of time," we don't want to
     * constrain it back again here. Therefore, the max value is never reduced here but at best initially set if it
     * was <code>null</code> before, or extended to a later point in time.
     */
    private void updateMinMax(RaceTimesInfoDTO newRaceTimesInfo) {
        Util.Pair<Date, Date> raceMinMax = RaceTimesCalculationUtil.caluclateRaceMinMax(timer, newRaceTimesInfo);
        
        Date min = raceMinMax.getA();
        Date max = raceMinMax.getB();
        
        // never reduce max if it was already set
        if (min != null && max != null && (getToTime() == null || getToTime().before(max))) {
            setMinMax(min, max, /* fireEvent */ false); // no event because we guarantee time to be between min and max
        }
    }
    
    /**
     * When in {@link PlayModes#Replay} mode, tries to put the {@link #timer} to the time point when the last leg was
     * finished first. If this time point is not (yet?) known, tries to put the {@link #timer} to the
     * {@link RaceTimesInfoDTO#endOfRace end of the race}. If that happens to be undefined (<code>null</code>), the
     * {@link RaceTimesInfoDTO#startOfRace start of the race} is used instead. If that isn't available either, the timer
     * remains unchanged.
     * <p>
     * 
     * When in {@link PlayModes#Live} mode, tries to advance the timer to the time point of the
     * {@link RaceTimesInfoDTO#timePointOfNewestEvent timePointOfNewestEvent} received from the tracking infrastructure
     * so far and puts the timer into {@link PlayStates#Playing play mode}.
     */
    private void initTimerPosition(RaceTimesInfoDTO newRaceTimesInfo) {
        // initialize timer position
        switch (timer.getPlayMode()) {
        case Live:
            if (newRaceTimesInfo.newestTrackingEvent != null) {
                timer.setTime(newRaceTimesInfo.newestTrackingEvent.getTime());
            }
            timer.play();
            break;
        case Replay:
            // set time to start of race
            if (newRaceTimesInfo.startOfRace != null) {
                timer.setTime(newRaceTimesInfo.startOfRace.getTime());
            }
            break;
        }
    }

    /**
     * Expected to be called before {@link #lastRaceTimesInfo} has been updated to <code>newRaceTimesInfo</code>
     */
    private void updateLegMarkers(RaceTimesInfoDTO newRaceTimesInfo) {
        boolean requiresMarkerUpdate = true;
        // updating the sliderbar markers requires a lot of time, therefore we need to do this only if required
        if (lastRaceTimesInfo != null && lastRaceTimesInfo.markPassingTimes.size() == newRaceTimesInfo.markPassingTimes.size()) {
            requiresMarkerUpdate = false;
            int numberOfLegs = newRaceTimesInfo.markPassingTimes.size();
            for (int i = 0; i < numberOfLegs; i++) {
                if (!Util.equalsWithNull(newRaceTimesInfo.markPassingTimes.get(i).firstPassingDate, lastRaceTimesInfo.markPassingTimes.get(i).firstPassingDate)) {
                    requiresMarkerUpdate = true;
                    break;
                }
            }
            if ((lastRaceTimesInfo.startOfRace == null && newRaceTimesInfo.startOfRace != null)
                    || (lastRaceTimesInfo.startOfRace != null && newRaceTimesInfo.startOfRace == null)
                    || (lastRaceTimesInfo.startOfRace != null && newRaceTimesInfo.startOfRace != null
                    && lastRaceTimesInfo.startOfRace.getTime() != newRaceTimesInfo.startOfRace.getTime())) {
                requiresMarkerUpdate = true;
            }
            if ((lastRaceTimesInfo.endOfRace == null && newRaceTimesInfo.endOfRace != null)
                    || (lastRaceTimesInfo.endOfRace != null && newRaceTimesInfo.endOfRace == null)
                    || (lastRaceTimesInfo.endOfRace != null && newRaceTimesInfo.endOfRace != null
                    && lastRaceTimesInfo.endOfRace.getTime() != newRaceTimesInfo.endOfRace.getTime())) {
                requiresMarkerUpdate = true;
            }
        }
        if (requiresMarkerUpdate && timeSlider.isMinMaxInitialized()) {
            redrawAllMarkers(newRaceTimesInfo);
        }
    }
    
    private void redrawAllMarkers(RaceTimesInfoDTO newRaceTimesInfo) {
        List<MarkPassingTimesDTO> markPassingTimes = newRaceTimesInfo.getMarkPassingTimes();
        timeSlider.clearMarkers();
        if (newRaceTimesInfo.startOfRace != null) {
            long markerTime = newRaceTimesInfo.startOfRace.getTime();
            if (!timeSlider.isZoomed() || (timeSlider.isZoomed() && markerTime > timeSlider.getMinValue() && markerTime < timeSlider.getMaxValue())) {
                timeSlider.addMarker("S", new Double(markerTime));
            }
        }
        int markPassingCounter = 1;
        for (MarkPassingTimesDTO markPassingTimesDTO: markPassingTimes) {
            // ignore the start mark passing
            if (markPassingCounter > 1 && markPassingTimesDTO.firstPassingDate != null) {
                long markerTime = markPassingTimesDTO.firstPassingDate.getTime();
                if (!timeSlider.isZoomed() || (timeSlider.isZoomed() && markerTime > timeSlider.getMinValue() && markerTime < timeSlider.getMaxValue())) {
                    timeSlider.addMarker(markPassingTimesDTO.getName(), new Double(markerTime));
                }
            }
            markPassingCounter++;
        }
        if (newRaceTimesInfo.endOfRace != null) {
            long markerTime = newRaceTimesInfo.endOfRace.getTime();
            if (!timeSlider.isZoomed() || (timeSlider.isZoomed() && markerTime > timeSlider.getMinValue() && markerTime < timeSlider.getMaxValue())) {
                timeSlider.addMarker("E", new Double(markerTime));
            }
        }
        timeSlider.redraw(); 
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        timer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        updateTimeInfo(raceTimesInfos.get(selectedRace));
    }
}
