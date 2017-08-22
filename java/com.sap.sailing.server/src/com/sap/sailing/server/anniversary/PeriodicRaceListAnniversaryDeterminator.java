package com.sap.sailing.server.anniversary;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util.Pair;

public class PeriodicRaceListAnniversaryDeterminator {
    private static final Logger logger = Logger.getLogger(PeriodicRaceListAnniversaryDeterminator.class.getName());

    private final MongoObjectFactory mongoObjectFactory;

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

    private final RacingEventService raceService;

    /**
     * Internal known races count, skip all updates, if the amount did not change
     */
    private int lastAmount;

    public interface AnniversaryChecker {
        /**
         * updates the internal state, required to be called if the raceCount has changed as else the getters will
         * return stale data, it is NOT allowed to call any during update
         * 
         * @param raceCount
         */
        void update(int raceCount);

        /**
         * Given the number of current Races, this method should return a List of all races that were anniversaries in
         * the past
         */
        List<Integer> getAnniversaries();

        /**
         * Given the number of current Races, this method should return the next Anniversary that will be detected by
         * this checker
         */
        Integer getNextAnniversary();

        AnniversaryType getType();
    }

    public PeriodicRaceListAnniversaryDeterminator(TypeBasedServiceFinderFactory serviceFinderFactory,
            RacingEventService raceService, AnniversaryChecker... checkerToUse) {
        this.raceService = raceService;
        knownAnniversaries = new ConcurrentHashMap<>();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(serviceFinderFactory);
        try {
            knownAnniversaries.putAll(mongoObjectFactory.getAnniversaryData());
        } catch (MalformedURLException e) {
            logger.warning("Could not load anniversaries from MongoDb");
        }
        checkers = new CopyOnWriteArrayList<>();
        for (AnniversaryChecker toAdd : checkerToUse) {
            checkers.add(toAdd);
        }
    }

    public void uponUpdate(Collection<SimpleRaceInfo> remoteRaces, Collection<SimpleRaceInfo> localRaces) {
        int amount = remoteRaces.size()+localRaces.size();
        if(amount != lastAmount){
            checkForNewAnniversaries(remoteRaces, localRaces);
        }
        lastAmount = amount;
    }

    private void checkForNewAnniversaries(Collection<SimpleRaceInfo> remoteRaces,
            Collection<SimpleRaceInfo> localRaces) {
        ArrayList<SimpleRaceInfo> allRaces = new ArrayList<>(remoteRaces);
        allRaces.addAll(localRaces);

        Collections.sort(allRaces, new Comparator<SimpleRaceInfo>() {

            @Override
            public int compare(SimpleRaceInfo o1, SimpleRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        boolean requiresPersist = false;
        Integer nearestNext = Integer.MAX_VALUE;
        AnniversaryType nearestType = null;
        for (AnniversaryChecker checker : checkers) {
            checker.update(allRaces.size());
            // find past anniversaries
            List<Integer> anniversaries = checker.getAnniversaries();
            for (Integer anniversary : anniversaries) {
                // adjust for zero started counting of the allRaceslist
                int index = anniversary - 1;
                if (!knownAnniversaries.contains(index)) {
                    SimpleRaceInfo anniversaryRace = allRaces.get(index);
                    insert(anniversary, anniversaryRace, checker.getType());
                    requiresPersist = true;
                }
            }
            // find next anniversaries
            Integer next = checker.getNextAnniversary();
            if (next != null && next < nearestNext) {
                nearestNext = next;
                nearestType = checker.getType();
            }
        }
        if (nearestNext.intValue() != Integer.MAX_VALUE) {
            nextAnniversaryNumber = new Pair<Integer, AnniversaryType>(nearestNext, nearestType);
        }
        currentRaceCount = allRaces.size();

        if (requiresPersist) {
            mongoObjectFactory.storeAnniversaryData(knownAnniversaries);
        }
    }

    private void insert(int anniversaryToCheck, SimpleRaceInfo simpleRaceInfo, AnniversaryType anniversaryType) {
        DetailedRaceInfo fullData = raceService.getFullDetailsForRaceCascading(simpleRaceInfo.getIdentifier());
        logger.info("Determined new Anniversary! " + anniversaryToCheck + " " + anniversaryType + " " + fullData);
        knownAnniversaries.put(anniversaryToCheck, new Pair<>(fullData, anniversaryType));
        // TODO replication here, to ensure all replicas have the exact same race as anniversary!
    }

    public Pair<Integer, AnniversaryType> getNextAnniversaryNumber() {
        return nextAnniversaryNumber;
    }

    public ConcurrentHashMap<Integer, Pair<DetailedRaceInfo, AnniversaryType>> getKnownAnniversaries() {
        return knownAnniversaries;
    }

    public Integer getCurrentRaceCount() {
        return currentRaceCount;
    }
}
