package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.LegTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceTimePanel extends TimePanel<RaceTimePanelSettings> implements RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private RaceIdentifier raceIdentifier;
    
    private long refreshIntervalTimeInfos = 3000;
    
    private boolean isTimerInitialized = false;
    
    private RaceTimesInfoDTO lastRaceTimesInfo;
    
    public RaceTimePanel(final SailingServiceAsync sailingService, Timer timer, ErrorReporter errorReporter, StringMessages stringMessages) {
        super(timer, stringMessages);
        
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        
        startAutoTimeInfoUpdate();
    }

    private void startAutoTimeInfoUpdate() {
        RepeatingCommand command = new RepeatingCommand() {
            @Override
            public boolean execute() {
                readRaceTimesInfo();
                return true;
//                if (!isTimeInfosComplete) {
//                    readRaceTimesInfo();
//                }
//                return timer.getPlayState() == PlayStates.Playing ? true: false;
            }
        };
        scheduleTimeInfoUpdateCommand(command);
    }

    private void scheduleTimeInfoUpdateCommand(RepeatingCommand command) {
        Scheduler.get().scheduleFixedPeriod(command, (int) refreshIntervalTimeInfos);
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        super.playStateChanged(playState, playMode);
        
        switch(playState) {
            case Playing:
                startAutoTimeInfoUpdate();
                break;
            case Paused:
            case Stopped:
                break;
        }
    }    
    
    @Override
    public void updateSettings(RaceTimePanelSettings newSettings) {
        super.updateSettings(newSettings);
        isTimerInitialized = false;
        readRaceTimesInfo();
    }

    @Override
    public RaceTimePanelSettings getSettings() {
        RaceTimePanelSettings result = new RaceTimePanelSettings();
        result.setDelayToLivePlayInSeconds(timer.getLivePlayDelayInMillis()/1000);
        result.setRefreshInterval(timer.getRefreshInterval());
        result.setRaceTimesInfo(lastRaceTimesInfo);
        return result;
    }

    @Override
    public SettingsDialogComponent<RaceTimePanelSettings> getSettingsDialogComponent() {
        return new RaceTimePanelSettingsDialogComponent(getSettings(), stringMessages);
    }

    private void updateTimeInfos(RaceTimesInfoDTO raceTimesInfo) {
        lastRaceTimesInfo = raceTimesInfo;
        if (raceTimesInfo == null) { 
            // in case the race is not tracked anymore we reset the timer
            reset();
        } else { 
            if (raceTimesInfo.startOfTracking != null && raceTimesInfo.timePointOfNewestEvent != null) {
                // we set here the min and max of the time slider, the start and end of the race as well as the known leg markers
                if (!isTimerInitialized) {
                    long livePlayDelayInMillis = timer.getLivePlayDelayInMillis();
                    long liveTimePointInMillis = System.currentTimeMillis() - livePlayDelayInMillis;
                    if (liveTimePointInMillis < raceTimesInfo.timePointOfNewestEvent.getTime() && 
                       liveTimePointInMillis > raceTimesInfo.startOfTracking.getTime()) {
                        timer.setPlayMode(PlayModes.Live);
                    } else {
                        timer.setPlayMode(PlayModes.Replay);
                    }
                    boolean minMaxInitialized = initMinMax(raceTimesInfo);
                    boolean timerPositionInitialized = initTimerPosition(raceTimesInfo);
                    if (minMaxInitialized && timerPositionInitialized) {
                        isTimerInitialized = true;
                    }
                }
                updateLegMarkers(raceTimesInfo);
            } else {
                // the tracked race did not start yet or has no events yet
                // maybe show a special state for this like "Race did not start yet"
            }
        }
    } 
    
    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            raceIdentifier = selectedRaces.iterator().next();
            isTimerInitialized = false;
            readRaceTimesInfo();
        }
    }

    /**
     * @return whether both, minimum and maximum were initialized using {@link #setMinMax(Date, Date)}
     */
    private boolean initMinMax(RaceTimesInfoDTO newRaceTimesInfo) {
        Date min = null;
        Date max = null;
        switch (timer.getPlayMode()) {
        case Live:
            if (newRaceTimesInfo.startOfTracking != null) {
                min = newRaceTimesInfo.startOfTracking;
            }
            if (newRaceTimesInfo.startOfRace != null) {
                min = new Date(newRaceTimesInfo.startOfRace.getTime() - 5 * 60 * 1000);
            }
            if (newRaceTimesInfo.timePointOfNewestEvent != null) {
                max = newRaceTimesInfo.timePointOfNewestEvent;
            }
            break;
        case Replay:
            if (newRaceTimesInfo.startOfTracking != null) {
                min = newRaceTimesInfo.startOfTracking;
            } else if (newRaceTimesInfo.startOfRace != null) {
                min = new Date(newRaceTimesInfo.startOfRace.getTime() - 5 * 60 * 1000);
            }
            if (newRaceTimesInfo.endOfRace != null) {
                max = newRaceTimesInfo.endOfRace;
            } else if (newRaceTimesInfo.timePointOfNewestEvent != null) {
                max = newRaceTimesInfo.timePointOfNewestEvent;
            }
            break;
        }
        if (min != null && max != null) {
            setMinMax(min, max);
            return true;
        }
        return false;
    }
    
    private boolean initTimerPosition(RaceTimesInfoDTO newRaceTimesInfo) {
        // initialize timer position
        switch (timer.getPlayMode()) {
        case Live:
            if (newRaceTimesInfo.timePointOfNewestEvent != null)
                timer.setTime(newRaceTimesInfo.timePointOfNewestEvent.getTime());
            timer.play();
            break;
        case Replay:
            if (newRaceTimesInfo.endOfRace != null) {
                timer.setTime(newRaceTimesInfo.endOfRace.getTime());
            } else {
                if (newRaceTimesInfo.startOfRace != null)
                    timer.setTime(newRaceTimesInfo.startOfRace.getTime());
            }
            // set time to end of race
            if (newRaceTimesInfo.getLastLegTimes() != null) {
                timer.setTime(newRaceTimesInfo.getLastLegTimes().firstPassingDate.getTime());
            }
            break;
        }
        return timer.getTime() != null;
    }

    private void updateLegMarkers(RaceTimesInfoDTO newRaceTimesInfo) {
        List<LegTimesInfoDTO> legTimepoints = newRaceTimesInfo.getLegTimes();
        if (sliderBar.isMinMaxInitialized()) {
            sliderBar.clearMarkers();
            for (LegTimesInfoDTO legTimepointDTO : legTimepoints) {
              sliderBar.addMarker(legTimepointDTO.name, new Double(legTimepointDTO.firstPassingDate.getTime()));
            }
            sliderBar.redraw();
        }
        // the marker information is complete if we have a time for the start, the end, and all legs
        // TODO: Implement this later on
        // TODO (from Axel Uhl) I think the above may not be correct. The data may change at all times, even after the race. isTimeInfoComplete removed. Frank, please review.
        // isTimeInfoComplete = false;
    }
    
    private void readRaceTimesInfo() {
        if (raceIdentifier != null) {
            sailingService.getRaceTimesInfo(raceIdentifier, new AsyncCallback<RaceTimesInfoDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error reading race timepoints: " + caught.getMessage());
                }

                @Override
                public void onSuccess(RaceTimesInfoDTO raceTimesInfo) {
                    // raceTimesInfo can be null if the race is not tracked anymore
                    updateTimeInfos(raceTimesInfo);
                }
            });
        }
    }
}
