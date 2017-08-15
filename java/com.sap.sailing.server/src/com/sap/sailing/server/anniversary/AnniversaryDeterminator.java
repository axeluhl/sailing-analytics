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
import com.sap.sailing.server.anniversary.AnniversaryCalculationScheduler.ChangeListener;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.osgi.CachedOsgiTypeBasedServiceFinderFactory;

public class AnniversaryDeterminator implements ChangeListener {
    private static final Logger logger = Logger.getLogger(AnniversaryDeterminator.class.getName());
    
    private MongoObjectFactory mongoObjectFactory;
    private AnniversaryCalculationScheduler anniversaryCalculator;

    ConcurrentHashMap<Integer, DetailedRaceInfo> knownAnniversaries;
    CopyOnWriteArrayList<AnniversaryChecker> checkers;
    
    public interface AnniversaryChecker{
        List<Pair<Integer, SimpleRaceInfo>> determineAnniversaries(List<SimpleRaceInfo> raceinfos);
    }

    public AnniversaryDeterminator(CachedOsgiTypeBasedServiceFinderFactory serviceFinderFactory,AnniversaryChecker... checkerToUse) {
        mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(serviceFinderFactory);
        try {
            knownAnniversaries.putAll(mongoObjectFactory.getAnniversaryData());
        } catch (MalformedURLException e) {
            logger.warning("Could not load anniversaries from MongoDb");
        }
        checkers = new CopyOnWriteArrayList<>();
        for(AnniversaryChecker toAdd:checkerToUse){
            checkers.add(toAdd);
        }
        knownAnniversaries = new ConcurrentHashMap<>();
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
        for(AnniversaryChecker checker:checkers){
            List<Pair<Integer,SimpleRaceInfo>> anniversaries = checker.determineAnniversaries(allRaces);
            for(Pair<Integer, SimpleRaceInfo> anniversary:anniversaries){
                boolean added = upsertAnniversary(anniversary.getA(),anniversary.getB());
                if(added){
                    requiresPersist = true;
                }
            }
        }
        if (requiresPersist) {
            mongoObjectFactory.storeAnniversaryData(knownAnniversaries);
        }
    }



    private boolean upsertAnniversary(int anniversaryToCheck, SimpleRaceInfo simpleRaceInfo) {
        DetailedRaceInfo knownAnniversary = knownAnniversaries.get(anniversaryToCheck);
        if (knownAnniversary != null) {
            System.out.println("Skipping anniversary " + anniversaryToCheck + " as it is already known");
            return false;
        } else {
            System.out.println("New anniversary " + anniversaryToCheck + " " + simpleRaceInfo);
            DetailedRaceInfo fullData = anniversaryCalculator.getFullAnniversaryData(simpleRaceInfo.getIdentifier());
            knownAnniversaries.put(anniversaryToCheck, fullData);
            return true;
        }
    }

    @Override
    public void setAnniversaryCalculator(AnniversaryCalculationScheduler anniversaryCalculator) {
        this.anniversaryCalculator = anniversaryCalculator;
    }
}
