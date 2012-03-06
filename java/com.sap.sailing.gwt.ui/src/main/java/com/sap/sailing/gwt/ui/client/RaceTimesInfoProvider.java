package com.sap.sailing.gwt.ui.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class RaceTimesInfoProvider {
    
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    
    private RaceIdentifier raceIdentifier;
    private long requestInterval;
    
    private RaceTimesInfoDTO raceTimesInfo;
    
    private Set<RaceTimesInfoProviderListener> listeners;

    public RaceTimesInfoProvider(SailingServiceAsync sailingService, ErrorReporter errorReporter, RaceIdentifier raceIdentifier, long requestInterval) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.raceIdentifier = raceIdentifier;
        this.requestInterval = requestInterval;
        raceTimesInfo = null;
        listeners = new HashSet<RaceTimesInfoProviderListener>();
        
        RepeatingCommand command = new RepeatingCommand() {
            @Override
            public boolean execute() {
                readTimesInfo();
                Scheduler.get().scheduleFixedPeriod(this, (int) RaceTimesInfoProvider.this.requestInterval);
                return false;
            }
        };
        Scheduler.get().scheduleFixedPeriod(command, (int) this.requestInterval);
    }
    
    public RaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }
    
    /**
     * Sets the race identifier and resets the time info to <code>null</code>.
     */
    public void setRaceIdentifier(RaceIdentifier raceIdentifier) {
        this.raceIdentifier = raceIdentifier;
        raceTimesInfo = null;
    }
    
    public long getRequestInterval() {
        return requestInterval;
    }
    
    public void setRequestInterval(long requestInterval){
        this.requestInterval = requestInterval;
    }
    
    /**
     * @return The {@link RaceTimesInfoDTO times info} for the current {@link #raceIdentifier}, or <code>null</code> if no time info is available.
     */
    public RaceTimesInfoDTO getRaceTimesInfo(){
        return raceTimesInfo;
    }
    
    public void addRaceTimesInfoChangeListener(RaceTimesInfoProviderListener listener) {
        listeners.add(listener);
    }
    
    public void removeRaceTimesInfoChangeListener(RaceTimesInfoProviderListener listener){
        listeners.remove(listener);
    }
    
    private void readTimesInfo() {
        if (raceIdentifier != null) {
            sailingService.getRaceTimesInfo(raceIdentifier, new AsyncCallback<RaceTimesInfoDTO>() {
                @Override
                public void onSuccess(RaceTimesInfoDTO raceTimesInfo) {
                    RaceTimesInfoProvider.this.raceTimesInfo = raceTimesInfo;
                    for (RaceTimesInfoProviderListener listener : listeners) {
                        listener.raceTimesInfoReceived(getRaceTimesInfo());
                    }
                }
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error reading race timepoints: " + caught.getMessage());
                }
            });
        }
    }
    
}
