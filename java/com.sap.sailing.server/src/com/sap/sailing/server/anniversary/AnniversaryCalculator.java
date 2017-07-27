package com.sap.sailing.server.anniversary;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.domain.anniversary.AnniversaryConflictResolver;
import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class AnniversaryCalculator implements Runnable {
    private static final long INITIAL_DELAY = 1000;
    private static final long DELAY = 5000;
    private RacingEventService racingEventService;
    private AnniversaryConflictResolver resolver = new AnniversaryConflictResolver();
    private int lastAmount = -1;
    private CopyOnWriteArraySet<ChangeListener> listeners = new CopyOnWriteArraySet<>();
    
    interface ChangeListener{
        void onChange(Collection<AnniversaryRaceInfo> collection);
    }
    
    public AnniversaryCalculator(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.scheduleWithFixedDelay(this,INITIAL_DELAY ,DELAY, TimeUnit.MILLISECONDS);
    }

    public void addListener(ChangeListener listener){
        listeners.add(listener);
    }
    
    public void removeListener(ChangeListener listener){
        listeners.remove(listener);
    }
    
    @Override
    public void run() {
        if(racingEventService==null || listeners.isEmpty()){
            return;
        }
        HashMap<RegattaAndRaceIdentifier, AnniversaryRaceInfo> local = getLocalResults();
        //add remotes with resolver checking
        if(lastAmount < local.size()){
            for(ChangeListener listener:listeners){
                listener.onChange(local.values());
            }
        }
    }
    
    private HashMap<RegattaAndRaceIdentifier, AnniversaryRaceInfo> getLocalResults() {
        HashMap<RegattaAndRaceIdentifier, AnniversaryRaceInfo> anniversaryRaceList = new HashMap<RegattaAndRaceIdentifier, AnniversaryRaceInfo>();
        for (Event event : racingEventService.getAllEvents()) {
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                                AnniversaryRaceInfo current = anniversaryRaceList.get(raceIdentifier);
                                AnniversaryRaceInfo raceInfo = new AnniversaryRaceInfo(raceIdentifier,
                                        leaderboard.getName(), trackedRace.getStartOfRace().asDate(),
                                        event.getId().toString(),null);
                                if(current == null){
                                    anniversaryRaceList.put(raceIdentifier, raceInfo);
                                }else{
                                    AnniversaryRaceInfo prefered = resolver.resolve(current,raceInfo);
                                    if(prefered != current){
                                        anniversaryRaceList.put(prefered.getIdentifier(), prefered);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return anniversaryRaceList;
    }

    public void setRacingEventService(RacingEventService racingEventService) {
        this.racingEventService = racingEventService;
    }

}
