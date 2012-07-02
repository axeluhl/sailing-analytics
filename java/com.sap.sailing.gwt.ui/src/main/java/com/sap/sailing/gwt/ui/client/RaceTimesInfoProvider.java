package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class RaceTimesInfoProvider {
    
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    
    private Set<RaceIdentifier> raceIdentifiers;
    private long requestInterval;
    
    private HashMap<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfos;
    
    private Set<RaceTimesInfoProviderListener> listeners;

    /**
     * The <code>raceIdentifiers</code> has to be <code>not-null</code>, but can be empty.
     */
    public RaceTimesInfoProvider(SailingServiceAsync sailingService, ErrorReporter errorReporter, Collection<RegattaAndRaceIdentifier> raceIdentifiers, long requestInterval) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.raceIdentifiers = new HashSet<RaceIdentifier>(raceIdentifiers);
        this.requestInterval = requestInterval;
        raceTimesInfos = new HashMap<RaceIdentifier, RaceTimesInfoDTO>();
        listeners = new HashSet<RaceTimesInfoProviderListener>();
        
        RepeatingCommand command = new RepeatingCommand() {
            @Override
            public boolean execute() {
                readTimesInfos();
                Scheduler.get().scheduleFixedPeriod(this, (int) RaceTimesInfoProvider.this.requestInterval);
                return false;
            }
        };
        Scheduler.get().scheduleFixedPeriod(command, (int) this.requestInterval);
        
        forceTimesInfosUpdate();
    }
    
    /**
     * @return An unmodifiable list of the RaceIdentifiers contained 
     */
    public Set<RaceIdentifier> getRaceIdentifiers() {
        return Collections.unmodifiableSet(raceIdentifiers);
    }
    
    /**
     * Adds the given {@link RaceIdentifier} and if <code>forceTimesInfoRequest</code> is <code>true</code>, an independent
     * request to fetch the time infos for the given race is forced. All listeners will receive a
     * {@link RaceTimesInfoProviderListener#raceTimesInfosReceived(Map)}.
     * 
     * @param raceIdentifier
     *            The {@link RaceIdentifier} to be added
     * @param forceTimesInfoRequest
     *            If <code>true</code> the race time info for the given race is fetched from the server
     */
    public void addRaceIdentifier(final RaceIdentifier raceIdentifier, boolean forceTimesInfoRequest) {
        raceIdentifiers.add(raceIdentifier);
        if (forceTimesInfoRequest) {
            sailingService.getRaceTimesInfo(raceIdentifier, new AsyncCallback<RaceTimesInfoDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to obtain the time infos for race "
                            + raceIdentifier.getRaceName() + ": " + caught.getMessage(), /* silentMode */ true);
                }

                @Override
                public void onSuccess(RaceTimesInfoDTO raceTimesInfo) {
                    if (raceTimesInfo != null) {
                        RaceTimesInfoProvider.this.raceTimesInfos.put(raceTimesInfo.getRaceIdentifier(), raceTimesInfo);
                        for (RaceTimesInfoProviderListener listener : listeners) {
                            listener.raceTimesInfosReceived(getRaceTimesInfos());
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Removes the given {@link RaceIdentifier} and the contained times info of this race.
     * @param raceIdentifier The {@link RaceIdentifier} to be removed
     */
    public void removeRaceIdentifier(RaceIdentifier raceIdentifier) {
        raceIdentifiers.remove(raceIdentifier);
        raceTimesInfos.remove(raceIdentifier);
    }
    
    public boolean containsRaceIdentifier(RaceIdentifier raceIdentifier) {
        return raceIdentifiers.contains(raceIdentifier);
    }
    
    /**
     * Clears the contained {@link RaceIdentifier} and the race time infos.
     */
    public void clearRaceIdentifiers() {
        raceIdentifiers.clear();
        raceTimesInfos.clear();
    }
    
    public long getRequestInterval() {
        return requestInterval;
    }
    
    /**
     * Sets the request interval. The new request interval will be used after the next call of the scheduled command.
     * @param requestInterval The new request interval
     */
    public void setRequestInterval(long requestInterval){
        this.requestInterval = requestInterval;
    }
    
    /**
     * @return An unmodifiable map of the {@link RaceTimesInfoDTO times infos} for the current {@link #raceIdentifiers},
     *         or an empty map if no time infos are available
     */
    public Map<RaceIdentifier, RaceTimesInfoDTO> getRaceTimesInfos(){
        return Collections.unmodifiableMap(raceTimesInfos);
    }
    
    public RaceTimesInfoDTO getRaceTimesInfo(RaceIdentifier raceIdentifier) {
        return raceTimesInfos.get(raceIdentifier);
    }
    
    public void addRaceTimesInfoProviderListener(RaceTimesInfoProviderListener listener) {
        listeners.add(listener);
    }
    
    public void removeRaceTimesInfoProviderListener(RaceTimesInfoProviderListener listener){
        listeners.remove(listener);
    }
    
    /**
     * Forces an independent request to fetch the time infos for all races. All listeners will receive a
     * {@link RaceTimesInfoProviderListener#raceTimesInfosReceived(Map)}.
     */
    public void forceTimesInfosUpdate() {
        readTimesInfos();
    }
    
    private void readTimesInfos() {
        if (!raceIdentifiers.isEmpty()) {
            sailingService.getRaceTimesInfos(raceIdentifiers, new AsyncCallback<List<RaceTimesInfoDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to obtain the race time infos: " + caught.getMessage(),
                            /* silentMode */ true);
                }

                @Override
                public void onSuccess(List<RaceTimesInfoDTO> raceTimesInfos) {
                    if (!raceTimesInfos.isEmpty()) {
                        for (RaceTimesInfoDTO raceTimesInfo : raceTimesInfos) {
                            RaceTimesInfoProvider.this.raceTimesInfos.put(raceTimesInfo.getRaceIdentifier(),
                                    raceTimesInfo);
                        }
                        for (RaceTimesInfoProviderListener listener : listeners) {
                            listener.raceTimesInfosReceived(getRaceTimesInfos());
                        }
                    }
                }
            });
        }
    }
    
}
