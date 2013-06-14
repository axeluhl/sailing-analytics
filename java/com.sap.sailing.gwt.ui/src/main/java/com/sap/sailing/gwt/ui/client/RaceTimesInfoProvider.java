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
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class RaceTimesInfoProvider {
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    
    private final Set<RegattaAndRaceIdentifier> raceIdentifiers;
    private long requestIntervalInMillis;
    
    private final HashMap<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos;
    
    private final Set<RaceTimesInfoProviderListener> listeners;
    
    /**
     * The <code>raceIdentifiers</code> has to be non-<code>null</code>, but can be empty.
     */
    public RaceTimesInfoProvider(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            Collection<RegattaAndRaceIdentifier> raceIdentifiers, long requestIntervalInMillis) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.raceIdentifiers = new HashSet<RegattaAndRaceIdentifier>(raceIdentifiers);
        this.requestIntervalInMillis = requestIntervalInMillis;
        raceTimesInfos = new HashMap<RegattaAndRaceIdentifier, RaceTimesInfoDTO>();
        listeners = new HashSet<RaceTimesInfoProviderListener>();
        RepeatingCommand command = new RepeatingCommand() {
            @Override
            public boolean execute() {
                readTimesInfos();
                Scheduler.get().scheduleFixedPeriod(this, (int) RaceTimesInfoProvider.this.requestIntervalInMillis);
                // don't execute *this* particular scheduled repeating command again; the line above re-scheduled already
                return false;
            }
        };
        command.execute();
    }
    
    /**
     * @return An unmodifiable list of the RegattaAndRaceIdentifiers contained 
     */
    public Set<RegattaAndRaceIdentifier> getRaceIdentifiers() {
        return Collections.unmodifiableSet(raceIdentifiers);
    }
    
    /**
     * Adds the given {@link RaceIdentifier} and if <code>forceTimesInfoRequest</code> is <code>true</code>, an independent
     * request to fetch the time infos for the given race is forced. All listeners will receive a
     * {@link RaceTimesInfoProviderListener#raceTimesInfosReceived(Map, long)}.
     * 
     * @param raceIdentifier
     *            The {@link RaceIdentifier} to be added
     * @param forceTimesInfoRequest
     *            If <code>true</code> the race time info for the given race is fetched from the server
     */
    public void addRaceIdentifier(final RegattaAndRaceIdentifier raceIdentifier, boolean forceTimesInfoRequest) {
        raceIdentifiers.add(raceIdentifier);
        if (forceTimesInfoRequest) {
            final long timePointWhenRequestWasSent = System.currentTimeMillis();
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
                        long millisecondsClientIsBehindServer = determineMillisecondsClientIsBehindServer(raceTimesInfo, timePointWhenRequestWasSent);
                        notifyListeners(millisecondsClientIsBehindServer);
                    }
                }
            });
        }
    }
    
    private void readTimesInfos() {
        if (!raceIdentifiers.isEmpty()) {
            final long timePointWhenRequestWasSent = System.currentTimeMillis();
            sailingService.getRaceTimesInfos(raceIdentifiers, new AsyncCallback<List<RaceTimesInfoDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to obtain the race time infos: " + caught.getMessage(),
                            /* silentMode */ true);
                }

                @Override
                public void onSuccess(List<RaceTimesInfoDTO> raceTimesInfos) {
                    if (!raceTimesInfos.isEmpty()) {
                        long millisecondsClientIsBehindServer = 0;
                        for (RaceTimesInfoDTO raceTimesInfo : raceTimesInfos) {
                            RaceTimesInfoProvider.this.raceTimesInfos.put(raceTimesInfo.getRaceIdentifier(), raceTimesInfo);
                            millisecondsClientIsBehindServer = determineMillisecondsClientIsBehindServer(raceTimesInfo, timePointWhenRequestWasSent);
                        }
                        notifyListeners(millisecondsClientIsBehindServer);
                    }
                }
            });
        }
    }

    /**
     * Each {@link RaceTimesInfoDTO} has the current server time when the object was filled in its
     * {@link RaceTimesInfoDTO#currentServerTime} field. Calculating the difference gives a good indication about
     * how far the client clock is off, compared to the server clock.
     */
    private long determineMillisecondsClientIsBehindServer(RaceTimesInfoDTO raceTimesInfo, long timePointWhenRequestWasSent) {
        // Let's assume the calculation of the RaceTimesInfoDTO objects during the request takes almost no time compared
        // to network latency. Then the difference between the client's current time and the time when the request was sent
        // can be considered network latency. If we furthermore assume that the network latency is roughly symmetrical for
        // request and response, dividing the total latency by two will approximately tell us the time that passed between
        // when the server set the RaceTimesInfoDTO.currentServerTime field and the current time.
        long now = System.currentTimeMillis();
        long responseNetworkLatencyInMillis = (now-timePointWhenRequestWasSent)/2l;
        return raceTimesInfo.currentServerTime.getTime() + responseNetworkLatencyInMillis - now;
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
        return requestIntervalInMillis;
    }
    
    /**
     * Sets the request interval. The new request interval will be used after the next call of the scheduled command.
     * @param requestInterval The new request interval
     */
    public void setRequestInterval(long requestInterval){
        this.requestIntervalInMillis = requestInterval;
    }
    
    /**
     * @return An unmodifiable map of the {@link RaceTimesInfoDTO times infos} for the current {@link #raceIdentifiers},
     *         or an empty map if no time infos are available
     */
    public Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> getRaceTimesInfos(){
        return Collections.unmodifiableMap(raceTimesInfos);
    }
    
    public RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier) {
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
     * {@link RaceTimesInfoProviderListener#raceTimesInfosReceived(Map, long)}.
     */
    public void forceTimesInfosUpdate() {
        readTimesInfos();
    }
    
    public RegattaAndRaceIdentifier getFirstStartedAndUnfinishedRace(LeaderboardDTO leaderboard) {
        RegattaAndRaceIdentifier firstStartedAndUnfinishedRace = null;
        Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos = getRaceTimesInfos();
        for (RaceColumnDTO race : leaderboard.getRaceList()) {
            for (FleetDTO fleet : race.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier(fleet);
                if (raceIdentifier != null) {
                    RaceTimesInfoDTO raceTimes = raceTimesInfos.get(raceIdentifier);
                    if (raceTimes != null && raceTimes.startOfTracking != null && raceTimes.endOfRace == null) {
                        firstStartedAndUnfinishedRace = raceIdentifier;
                        break;
                    }
                }
            }
        }
        return firstStartedAndUnfinishedRace;
    }

    private void notifyListeners(long millisecondsClientIsBehindServer) {
        for (RaceTimesInfoProviderListener listener : listeners) {
            listener.raceTimesInfosReceived(getRaceTimesInfos(), millisecondsClientIsBehindServer);
        }
    }
    
}
