package com.sap.sailing.server.anniversary;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RemoteSailingServerSet;
import com.sap.sse.common.Util.Pair;

public class PeriodicRaceListAnniversaryDeterminator {
    private static final Logger logger = Logger.getLogger(PeriodicRaceListAnniversaryDeterminator.class.getName());

    private final ConcurrentHashMap<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnniversaries;
    private final CopyOnWriteArrayList<AnniversaryChecker> checkers;

    /**
     * Contains the results of the last calculation, a number giving the next anniversary
     */
    private Pair<Integer, AnniversaryType> nextAnniversaryNumber;

    /**
     * Contains the results of the last calculation, a number giving the amount of races existing
     */
    private Integer currentRaceCount;

    private final RacingEventService raceEventService;
    private final RemoteSailingServerSet remoteSailingServerSet;
    private final Runnable raceChangedListener;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public interface AnniversaryChecker {

        /**
         * Updates the internal state based and is required to be called on any race count change to avoid stale data.
         * <b>NOTE: </b>The getter methods for {@link #getAnniversaries() past} or {@link #getNextAnniversary() next}
         * anniversaries may only be called after the update is processed.
         * 
         * @param raceCount
         *            the total number of races
         */
        void update(int raceCount);

        /**
         * Given the {@link #update(int) current number of races}, this method should return a list containing all past
         * anniversary numbers.
         * 
         * @return a list of all past anniversary numbers
         */
        List<Integer> getAnniversaries();

        /**
         * Given the {@link #update(int) current number of races}, this method should provide the next anniversary.
         * 
         * @return the next anniversary number, or <code>null</code> if next anniversary cannot be determined
         */
        Integer getNextAnniversary();

        /**
         * Provides the {@link AnniversaryType type} of the {@link AnniversaryChecker}.
         * 
         * @return the {@link AnniversaryChecker}'s {@link AnniversaryType type}
         */
        AnniversaryType getType();
    }

    public PeriodicRaceListAnniversaryDeterminator(RacingEventService raceEventService,
            RemoteSailingServerSet remoteSailingServerSet, AnniversaryChecker... checkerToUse) {
        this.raceEventService = raceEventService;
        this.remoteSailingServerSet = remoteSailingServerSet;
        this.knownAnniversaries = new ConcurrentHashMap<>();

        try {
            knownAnniversaries.putAll(raceEventService.getDomainObjectFactory().getAnniversaryData());
        } catch (MalformedURLException e) {
            logger.warning("Could not load anniversaries from MongoDb");
        }
        checkers = new CopyOnWriteArrayList<>();
        for (AnniversaryChecker toAdd : checkerToUse) {
            checkers.add(toAdd);
        }
        raceChangedListener = this::update;
        start();
    }

    private void update() {
        if (isStarted.get()) {
            // All races need to be passed through this map to eliminate duplicates based on the RegattaAndRaceIdentifier
            final Map<RegattaAndRaceIdentifier, SimpleRaceInfo> allRaces = new HashMap<>();
            remoteSailingServerSet.getCachedRaceList().forEach((remoteServer, result) -> {
                if (result.getB() != null) {
                    logger.warning("Could not update anniversary determinator, because remote server "
                            + remoteServer.getURL() + " returned error " + result.getB());
                } else {
                    result.getA().forEach(race -> allRaces.put(race.getIdentifier(), race));
                }
            });
            allRaces.putAll(raceEventService.getLocalRaceList());
            if (currentRaceCount == null || allRaces.size() != currentRaceCount) {
                checkForNewAnniversaries(allRaces);
            }
        }
    }

    private void checkForNewAnniversaries(Map<RegattaAndRaceIdentifier, SimpleRaceInfo> races) {
        if (isStarted.get()) {
            final ArrayList<SimpleRaceInfo> allRaces = new ArrayList<>(races.values());
            Collections.sort(allRaces, new Comparator<SimpleRaceInfo>() {
                @Override
                public int compare(SimpleRaceInfo o1, SimpleRaceInfo o2) {
                    return o1.getStartOfRace().compareTo(o2.getStartOfRace());
                }
            });
            
            final Map<Integer, AnniversaryType> requiredAnniversaries = new HashMap<>();
            Integer nearestNext = null;
            AnniversaryType nearestType = null;
            for (AnniversaryChecker checker : checkers) {
                checker.update(allRaces.size());
                // find past anniversaries
                for (Integer anniversary : checker.getAnniversaries()) {
                    requiredAnniversaries.putIfAbsent(anniversary, checker.getType());
                }
                // find next anniversaries
                Integer next = checker.getNextAnniversary();
                if (next != null && (nearestNext == null || next < nearestNext)) {
                    nearestNext = next;
                    nearestType = checker.getType();
                }
            }
            
            synchronized (this) {
                if (nearestNext.intValue() != Integer.MAX_VALUE) {
                    nextAnniversaryNumber = new Pair<Integer, AnniversaryType>(nearestNext, nearestType);
                }
                
                boolean requiresPersist = false;
                for (Map.Entry<Integer, AnniversaryType> anniversaryEntry : requiredAnniversaries.entrySet()) {
                    final Integer anniversary = anniversaryEntry.getKey();
                    if (!knownAnniversaries.containsKey(anniversary)) {
                        // adjust for zero started counting of the allRaceslist
                        final SimpleRaceInfo anniversaryRace = allRaces.get(anniversary - 1);
                        insert(anniversary, anniversaryRace, anniversaryEntry.getValue());
                        requiresPersist = true;
                    }
                }
                currentRaceCount = allRaces.size();
                if (requiresPersist) {
                    raceEventService.getMongoObjectFactory().storeAnniversaryData(knownAnniversaries);
                }
            }
        }
    }

    private void insert(int anniversaryToCheck, SimpleRaceInfo simpleRaceInfo, AnniversaryType anniversaryType) {
        DetailedRaceInfo fullData = raceEventService.getFullDetailsForRaceCascading(simpleRaceInfo.getIdentifier());
        logger.info("Determined new Anniversary! " + anniversaryToCheck + " - " + anniversaryType + " - " + fullData);
        final Pair<DetailedRaceInfo, AnniversaryType> anniversaryData = new Pair<>(fullData, anniversaryType);
        raceEventService.apply(new AddAnniversaryOperation(anniversaryToCheck, anniversaryData));
    }

    synchronized void addAnniversary(int anniversaryToCheck, final Pair<DetailedRaceInfo, AnniversaryType> anniversaryData) {
        knownAnniversaries.put(anniversaryToCheck, anniversaryData);
    }

    public Pair<Integer, AnniversaryType> getNextAnniversaryNumber() {
        return nextAnniversaryNumber;
    }

    public Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> getKnownAnniversaries() {
        return new HashMap<>(knownAnniversaries);
    }
    
    public synchronized void setKnownAnniversaries(Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversaries) {
        knownAnniversaries.clear();
        if (anniversaries != null) {
            knownAnniversaries.putAll(anniversaries);
        }
    }

    public Integer getCurrentRaceCount() {
        return currentRaceCount;
    }
    
    public void start() {
        isStarted.set(true);
        remoteSailingServerSet.addRemoteRaceResultReceivedCallback(raceChangedListener);
    }
    
    public synchronized void clearAndStop() {
        isStarted.set(false);
        remoteSailingServerSet.removeRemoteRaceResultReceivedCallback(raceChangedListener);
        clear();
    }

    public synchronized void clear() {
        knownAnniversaries.clear();
        nextAnniversaryNumber = null;
        currentRaceCount = null;
    }
}
