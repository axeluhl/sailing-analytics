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
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.server.anniversary.PeriodicRaceListCalculationScheduler.PeriodicRaceListCalculator;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;

public class PeriodicRaceListAnniversaryDeterminator implements PeriodicRaceListCalculator {
    private static final Logger logger = Logger.getLogger(PeriodicRaceListAnniversaryDeterminator.class.getName());

    private final MongoObjectFactory mongoObjectFactory;
    private PeriodicRaceListCalculationScheduler anniversaryCalculator;

    private final ConcurrentHashMap<Integer, DetailedRaceInfo> knownAnniversaries;
    private final CopyOnWriteArrayList<AnniversaryChecker> checkers;

    /**
     * Contains the results of the last calculation, a number giving the next anniversary
     */
    private Integer nextAnniversaryNumber;
    /**
     * Contains the results of the last calculation, a number giving the amount of races existing
     */
    private Integer currentRaceCount;

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
    }

    public PeriodicRaceListAnniversaryDeterminator(CachedOsgiTypeBasedServiceFinderFactory serviceFinderFactory,
            AnniversaryChecker... checkerToUse) {
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

    @Override
    public void uponUpdate(Collection<SimpleRaceInfo> collection) {
        ArrayList<SimpleRaceInfo> allRaces = new ArrayList<>(collection);
        
        Collections.sort(allRaces, new Comparator<SimpleRaceInfo>() {

            @Override
            public int compare(SimpleRaceInfo o1, SimpleRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        boolean requiresPersist = false;
        Integer nearestNext = Integer.MAX_VALUE;
        for (AnniversaryChecker checker : checkers) {
            checker.update(allRaces.size());
            // find past anniversaries
            List<Integer> anniversaries = checker.getAnniversaries();
            for (Integer anniversary : anniversaries) {
                //adjust for zero started counting of the allRaceslist
                int index = anniversary - 1;
                if (!knownAnniversaries.contains(index)) {
                    SimpleRaceInfo anniversaryRace = allRaces.get(index);
                    insert(anniversary, anniversaryRace);
                    requiresPersist = true;
                }
            }
            // find next anniversaries
            Integer next = checker.getNextAnniversary();
            if (next != null && next < nearestNext) {
                nearestNext = next;
            }
        }
        if(nearestNext.intValue() != Integer.MAX_VALUE){
            nextAnniversaryNumber = nearestNext;
        }
        currentRaceCount = allRaces.size();

        if (requiresPersist) {
            mongoObjectFactory.storeAnniversaryData(knownAnniversaries);
        }
    }

    private void insert(int anniversaryToCheck, SimpleRaceInfo simpleRaceInfo) {
        System.out.println("New anniversary " + anniversaryToCheck + " " + simpleRaceInfo);
        DetailedRaceInfo fullData = anniversaryCalculator.getFullAnniversaryData(simpleRaceInfo.getIdentifier());
        knownAnniversaries.put(anniversaryToCheck, fullData);
        // TODO replication here, to ensure all replicas have the exact same race as anniversary!
    }

    @Override
    public void setCalculator(PeriodicRaceListCalculationScheduler anniversaryCalculator) {
        this.anniversaryCalculator = anniversaryCalculator;
    }
    
    public Integer getNextAnniversaryNumber() {
        return nextAnniversaryNumber;
    }
    
    public ConcurrentHashMap<Integer, DetailedRaceInfo> getKnownAnniversaries() {
        return knownAnniversaries;
    }
    
    public Integer getCurrentRaceCount() {
        return currentRaceCount;
    }
}
