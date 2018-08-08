package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import com.sap.sailing.gwt.ui.actions.GetRaceTimesInfoAction;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class RaceTimesInfoProvider {
    
    private final SailingServiceAsync sailingService;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ErrorReporter errorReporter;
    
    private final Set<RegattaAndRaceIdentifier> raceIdentifiers;
    private long requestIntervalInMillis;
    
    private final HashMap<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos;
    private final Set<RaceTimesInfoProviderListener> listeners;
    private boolean terminated = false;
    
    private Map<RegattaAndRaceIdentifier, TimePoint> latestReceivedTagTimes;
    
    /**
     * The <code>raceIdentifiers</code> has to be non-<code>null</code>, but can be empty.
     */
    public RaceTimesInfoProvider(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter,
            Collection<RegattaAndRaceIdentifier> raceIdentifiers, long requestIntervalInMillis) {
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        this.raceIdentifiers = new HashSet<RegattaAndRaceIdentifier>(raceIdentifiers);
        this.requestIntervalInMillis = requestIntervalInMillis;
        raceTimesInfos = new HashMap<RegattaAndRaceIdentifier, RaceTimesInfoDTO>();
        listeners = new HashSet<RaceTimesInfoProviderListener>();
        latestReceivedTagTimes = new HashMap<RegattaAndRaceIdentifier, TimePoint>();
        RepeatingCommand command = new RepeatingCommand() {
            @Override
            public boolean execute() {
                if(!terminated){
                    readTimesInfos();
                    Scheduler.get().scheduleFixedPeriod(this, (int) RaceTimesInfoProvider.this.requestIntervalInMillis);
                    // don't execute *this* particular scheduled repeating command again; the line above re-scheduled already
                }
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
     * {@link RaceTimesInfoProviderListener#raceTimesInfosReceived(Map, long, Date, long)}.
     * 
     * @param raceIdentifier
     *            The {@link RaceIdentifier} to be added
     * @param forceTimesInfoRequest
     *            If <code>true</code> the race time info for the given race is fetched from the server
     */
    public void addRaceIdentifier(final RegattaAndRaceIdentifier raceIdentifier, boolean forceTimesInfoRequest) {
        raceIdentifiers.add(raceIdentifier);
        if (forceTimesInfoRequest) {
            final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
            sailingService.getRaceTimesInfo(raceIdentifier, latestReceivedTagTimes.get(raceIdentifier), new AsyncCallback<RaceTimesInfoDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to obtain the time infos for race "
                            + raceIdentifier.getRaceName() + ": " + caught.getMessage(), /* silentMode */ true);
                }

                @Override
                public void onSuccess(RaceTimesInfoDTO raceTimesInfo) {
                    final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                    if (raceTimesInfo != null) {
                        RaceTimesInfoProvider.this.raceTimesInfos.put(raceTimesInfo.getRaceIdentifier(), raceTimesInfo);
                        notifyListeners(clientTimeWhenRequestWasSent, raceTimesInfo.currentServerTime, clientTimeWhenResponseWasReceived);
                    }
                }
            });
        }
    }
    
    private void readTimesInfos() {
        if (!raceIdentifiers.isEmpty()) {
            final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
            GetRaceTimesInfoAction getRaceTimesInfoAction = new GetRaceTimesInfoAction(sailingService, raceIdentifiers, latestReceivedTagTimes);
            asyncActionsExecutor.execute(getRaceTimesInfoAction, new AsyncCallback<List<RaceTimesInfoDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to obtain the race time infos: " + caught.getMessage(),
                            /* silentMode */ true);
                }

                @Override
                public void onSuccess(List<RaceTimesInfoDTO> raceTimesInfos) {
                    final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                    if (!raceTimesInfos.isEmpty()) {
                        Date currentServerTime = null;
                        for (RaceTimesInfoDTO raceTimesInfo : raceTimesInfos) {
                            RaceTimesInfoProvider.this.raceTimesInfos.put(raceTimesInfo.getRaceIdentifier(), raceTimesInfo);
                            currentServerTime = raceTimesInfo.currentServerTime;
                        }
                        notifyListeners(clientTimeWhenRequestWasSent, currentServerTime, clientTimeWhenResponseWasReceived);
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
     * {@link RaceTimesInfoProviderListener#raceTimesInfosReceived(Map, long, Date, long)}.
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

    private Set<RaceTimesInfoProviderListener> getListeners() {
        synchronized (listeners) {
            return new HashSet<RaceTimesInfoProviderListener>(listeners);
        }
    }

    private void notifyListeners(long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        for (RaceTimesInfoProviderListener listener : getListeners()) {
            listener.raceTimesInfosReceived(getRaceTimesInfos(), clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
        }
    }
    
    public void terminate(){
        terminated = true;
        listeners.clear();
        raceIdentifiers.clear();
        raceTimesInfos.clear();
    }

    public TimePoint getLatestReceivedTag(RegattaAndRaceIdentifier raceIdentifier) {
        return latestReceivedTagTimes.get(raceIdentifier);
    }
    
    public void setLatestReceivedTagTime(RegattaAndRaceIdentifier raceIdentifier, TimePoint latestReceivedTagTime) {
        latestReceivedTagTimes.put(raceIdentifier, latestReceivedTagTime);
    }
}
