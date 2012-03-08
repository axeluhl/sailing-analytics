package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.LegTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceTimePanel extends TimePanel<RaceTimePanelSettings> implements RaceSelectionChangeListener, RaceTimesInfoProviderListener {
    private RaceTimesInfoProvider raceTimesInfoProvider;
    private RaceIdentifier selectedRace;
    
    public RaceTimePanel(Timer timer, StringMessages stringMessages, RaceTimesInfoProvider raceTimesInfoProvider) {
        super(timer, stringMessages);
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        selectedRace = null;
    }
    
    @Override
    public void updateSettings(RaceTimePanelSettings newSettings) {
        super.updateSettings(newSettings);
        raceTimesInfoProvider.setRequestInterval(newSettings.getRefreshInterval());
    }

    @Override
    public RaceTimePanelSettings getSettings() {
        RaceTimePanelSettings result = new RaceTimePanelSettings();
        result.setDelayToLivePlayInSeconds(timer.getLivePlayDelayInMillis()/1000);
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
            // in case the race is not tracked anymore we reset the timer
            reset();
        } else { 
            if (raceTimesInfo.startOfTracking != null && raceTimesInfo.timePointOfNewestEvent != null) {
                // we set here the min and max of the time slider, the start and end of the race as well as the known
                // leg markers
                boolean liveModeToBeMadePossible = isLiveModeToBeMadePossible();
                setLiveGenerallyPossible(liveModeToBeMadePossible);
                setJumpToLiveEnablement(liveModeToBeMadePossible && timer.getPlayMode() != PlayModes.Live);
                boolean timerAlreadyInitialized = getMin() != null && getMax() != null && sliderBar.getCurrentValue() != null;
                initMinMax(raceTimesInfo);
                if (!timerAlreadyInitialized) {
                    initTimerPosition(raceTimesInfo);
                }
                updateLegMarkers(raceTimesInfo);
            } else {
                // the tracked race did not start yet or has no events yet
                // maybe show a special state for this like "Race did not start yet"
            }
        }
    } 
    
    @Override
    protected boolean isLiveModeToBeMadePossible() {
        long livePlayDelayInMillis = timer.getLivePlayDelayInMillis();
        long eventTimeoutTolerance = 30 * 1000; // 30s 
        long liveTimePointInMillis = System.currentTimeMillis() - livePlayDelayInMillis;
        RaceTimesInfoDTO lastRaceTimesInfo = raceTimesInfoProvider != null ? raceTimesInfoProvider.getRaceTimesInfo(selectedRace) : null;
        return lastRaceTimesInfo != null &&
                lastRaceTimesInfo.timePointOfNewestEvent != null &&
                liveTimePointInMillis < lastRaceTimesInfo.timePointOfNewestEvent.getTime() + eventTimeoutTolerance &&
                lastRaceTimesInfo.startOfTracking != null &&
                liveTimePointInMillis > lastRaceTimesInfo.startOfTracking.getTime();
    }
    
    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            selectedRace = selectedRaces.iterator().next();
            if (!selectedRace.equals(raceTimesInfoProvider.getRaceIdentifiers())) {
                raceTimesInfoProvider.clearRaceIdentifiers();
                raceTimesInfoProvider.addRaceIdentifier(selectedRace, true);
            }
        }
    }

    /**
     * Obtains the min/max range for the slider bar from <code>newRaceTimesInfo</code> and adjusts the slider bar if
     * needed. Since we extend the slider bar to the right when playing across the "end of time," we don't want to
     * constrain it back again here. Therefore, the max value is never reduced here but at best initially set if it
     * was <code>null</code> before, or extended to a later point in time.
     */
    private void initMinMax(RaceTimesInfoDTO newRaceTimesInfo) {
        Date min = null;
        Date max = null;

        switch (timer.getPlayMode()) {
        case Live:
            if (newRaceTimesInfo.startOfRace != null) {
                long extensionTime = calculateRaceExtensionTime(newRaceTimesInfo.startOfRace, newRaceTimesInfo.timePointOfNewestEvent);
                
                min = new Date(newRaceTimesInfo.startOfRace.getTime() - extensionTime);
            } else if (newRaceTimesInfo.startOfTracking != null) {
                min = newRaceTimesInfo.startOfTracking;
            }
            
            if (newRaceTimesInfo.timePointOfNewestEvent != null) {
                max = newRaceTimesInfo.timePointOfNewestEvent;
            }
            break;
        case Replay:
            //TODO Merge with Franks branch for better end of race calculation
            Date tempEndOfRace = newRaceTimesInfo.getLastLegTimes() != null ? newRaceTimesInfo.getLastLegTimes().firstPassingDate : newRaceTimesInfo.endOfRace;
            long extensionTime = calculateRaceExtensionTime(newRaceTimesInfo.startOfRace, tempEndOfRace);
            
            if (newRaceTimesInfo.startOfRace != null) {
                min = new Date(newRaceTimesInfo.startOfRace.getTime() - extensionTime);
            } else if (newRaceTimesInfo.startOfTracking != null) {
                min = newRaceTimesInfo.startOfTracking;
            }
            
            if (newRaceTimesInfo.endOfRace != null) {
                max = new Date(tempEndOfRace.getTime() + extensionTime);
            } else if (newRaceTimesInfo.timePointOfNewestEvent != null) {
                max = newRaceTimesInfo.timePointOfNewestEvent;
            }
            break;
        }
        // never reduce max if it was already set
        if (min != null && max != null && (getMax() == null || getMax().before(max))) {
            setMinMax(min, max, /* fireEvent */ false); // no event because we guarantee time to be between min and max
        }
    }
    
    private long calculateRaceExtensionTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 5 * 60 * 1000; //5 minutes
        }
        
        long minExtensionTime = 60 * 1000; // 1 minute
        long maxExtensionTime = 10 * 60 * 1000; // 10 minutes
        double extensionTimeFactor = 0.1; // 10 percent of the overall race length
        long extensionTime = (long) ((endTime.getTime() - startTime.getTime()) * extensionTimeFactor);
        
        return extensionTime < minExtensionTime ? minExtensionTime : extensionTime > maxExtensionTime ? maxExtensionTime : extensionTime;
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
    private boolean initTimerPosition(RaceTimesInfoDTO newRaceTimesInfo) {
        // initialize timer position
        switch (timer.getPlayMode()) {
        case Live:
            if (newRaceTimesInfo.timePointOfNewestEvent != null) {
                timer.setTime(newRaceTimesInfo.timePointOfNewestEvent.getTime());
            }
            timer.play();
            break;
        case Replay:
            // set time to end of race
            if (newRaceTimesInfo.getLastLegTimes() != null) {
                timer.setTime(newRaceTimesInfo.getLastLegTimes().firstPassingDate.getTime());
            } else  if (newRaceTimesInfo.endOfRace != null) {
                timer.setTime(newRaceTimesInfo.endOfRace.getTime());
            } else  if (newRaceTimesInfo.startOfRace != null) {
                timer.setTime(newRaceTimesInfo.startOfRace.getTime());
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
    
    @Override
    public void raceTimesInfosReceived(Map<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfos) {
      // raceTimesInfo can be null if the race is not tracked anymore
      updateTimeInfo(raceTimesInfos.get(selectedRace));
    }
}
