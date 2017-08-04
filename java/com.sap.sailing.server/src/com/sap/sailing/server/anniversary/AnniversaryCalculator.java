package com.sap.sailing.server.anniversary;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import com.sap.sailing.domain.anniversary.SimpleAnniversaryRaceInfo;
import com.sap.sailing.server.RacingEventService;

//import java.util.Collection;
//import java.util.HashMap;
//import java.util.concurrent.CopyOnWriteArraySet;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Logger;
//
//import com.sap.sailing.domain.anniversary.AnniversaryConflictResolver;
//import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
//import com.sap.sailing.domain.anniversary.SimpleAnniversaryRaceInfo;
//import com.sap.sailing.domain.base.Event;
//import com.sap.sailing.domain.base.Fleet;
//import com.sap.sailing.domain.base.RaceColumn;
//import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
//import com.sap.sailing.domain.leaderboard.Leaderboard;
//import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
//import com.sap.sailing.domain.tracking.TrackedRace;
//import com.sap.sailing.server.RacingEventService;
//
public class AnniversaryCalculator implements Runnable {
//    private static final Logger logger = Logger.getLogger(AnniversaryCalculator.class.getName());
//    private static final long INITIAL_DELAY = 1000;
//    private static final long DELAY = 50000;
    private RacingEventService racingEventService;
//    private int lastAmount = -1;
//    // keep in memory, to ensure, that a offline going remoteservers races are still considered
//    HashMap<RegattaAndRaceIdentifier, SimpleAnniversaryRaceInfo> store = new HashMap<>();
//
//    private CopyOnWriteArraySet<ChangeListener> listeners = new CopyOnWriteArraySet<>();
//    private AnniversaryConflictResolver resolver = new AnniversaryConflictResolver();
//
    interface ChangeListener {
        void onChange(Collection<SimpleAnniversaryRaceInfo> collection);

        void setAnniversaryCalculator(AnniversaryCalculator anniversaryCalculator);
    }
//
    public AnniversaryCalculator(ScheduledExecutorService scheduledExecutorService) {
//        scheduledExecutorService.scheduleWithFixedDelay(this, INITIAL_DELAY, DELAY, TimeUnit.MILLISECONDS);
    }
//
    public void addListener(ChangeListener listener) {
//        listener.setAnniversaryCalculator(this);
//        listeners.add(listener);
    }
//
//    public void removeListener(ChangeListener listener) {
//        listener.setAnniversaryCalculator(null);
//        listeners.remove(listener);
//    }
//
    @Override
    public void run() {
//        try {
//            if (racingEventService == null || listeners.isEmpty()) {
//                return;
//            }
//            racingEventService.getRemoteRaceList(store);
//            getLocalResults(store);
//
//            // add remotes with resolver checking
//            for (ChangeListener listener : listeners) {
//                listener.onChange(store.values());
//            }
//        } catch (Exception e) {
//            logger.warning("Could not update anniversaries! ");
//            e.printStackTrace();
//        }
    }
//
//    private void getLocalResults(HashMap<RegattaAndRaceIdentifier, SimpleAnniversaryRaceInfo> store) {
//        for (Event event : racingEventService.getAllEvents()) {
//            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
//                for (Leaderboard leaderboard : group.getLeaderboards()) {
//                    for (RaceColumn race : leaderboard.getRaceColumns()) {
//                        for (Fleet fleet : race.getFleets()) {
//                            TrackedRace trackedRace = race.getTrackedRace(fleet);
//                            if (trackedRace != null) {
//                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
//                                store.put(raceIdentifier, new SimpleAnniversaryRaceInfo(raceIdentifier,
//                                        trackedRace.getStartOfRace().asDate()));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
    public void setRacingEventService(RacingEventService racingEventService) {
        this.racingEventService = racingEventService;
    }
//
//    public AnniversaryRaceInfo getFullAnniversaryDataLocal(SimpleAnniversaryRaceInfo simpleAnniversaryRaceInfo) {
//        AnniversaryRaceInfo match = null;
//        for (Event event : racingEventService.getAllEvents()) {
//            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
//                for (Leaderboard leaderboard : group.getLeaderboards()) {
//                    for (RaceColumn race : leaderboard.getRaceColumns()) {
//                        for (Fleet fleet : race.getFleets()) {
//                            TrackedRace trackedRace = race.getTrackedRace(fleet);
//                            if (trackedRace != null) {
//                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
//                                if (raceIdentifier.equals(simpleAnniversaryRaceInfo.getIdentifier())) {
//                                    AnniversaryRaceInfo newMatch = new AnniversaryRaceInfo(raceIdentifier,
//                                            leaderboard.getName(), trackedRace.getStartOfRace().asDate(),
//                                            event.getId());
//                                    if (match == null) {
//                                        match = newMatch;
//                                    } else {
//                                        match = resolver.resolve(match, newMatch);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return match;
//    }
//
//    private AnniversaryRaceInfo getFullAnniversaryDataRemote(SimpleAnniversaryRaceInfo simpleAnniversaryRaceInfo) {
//        // TODO read data from remote server sync
//        return null;
//    }
//
//    public AnniversaryRaceInfo getFullAnniversaryData(SimpleAnniversaryRaceInfo simpleAnniversaryRaceInfo) {
//        if (simpleAnniversaryRaceInfo.getRemoteName() == null) {
//            return getFullAnniversaryDataLocal(simpleAnniversaryRaceInfo);
//        } else {
//            return getFullAnniversaryDataRemote(simpleAnniversaryRaceInfo);
//        }
//    }
}
