package com.sap.sailing.server.anniversary;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.impl.RemoteSailingServerSet;
import com.sap.sailing.server.interfaces.AnniversaryRaceDeterminator;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * This class implements the calculation and management of anniversary races. The base of the anniversary calculation is
 * a list of {@link SimpleRaceInfo races} consisting of known remote races taken from {@link RemoteSailingServerSet} and
 * the set of locally available races taken from {@link RacingEventService}. The anniversaries to be found are defined
 * by a set of {@link AnniversaryChecker} instances given to the constructor. On every
 * {@link AnniversaryRaceDeterminatorImpl#update() update} a list of all known races is created and ordered by the
 * startTimePoint. With this, anniversaries are defined by their startTimePoint at the moment when the list exceeds a
 * number that matches an anniversary to be found. Even if an older race is added afterwards, the initially determined
 * nth race will stay the same.
 */
public class AnniversaryRaceDeterminatorImpl implements AnniversaryRaceDeterminator {
    private static final Logger logger = Logger.getLogger(AnniversaryRaceDeterminatorImpl.class.getName());

    private final ConcurrentHashMap<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnniversaries;
    private final CopyOnWriteArrayList<AnniversaryChecker> checkers;
    private final RacingEventService racingEventService;
    private final RemoteSailingServerSet remoteSailingServerSet;
    private final Runnable raceChangedListener;
    private final AtomicBoolean isStarted;

    private volatile Pair<Integer, AnniversaryType> nextAnniversary;
    private volatile int currentRaceCount;

    /**
     * Interface for checker classes which are passed to the {@link AnniversaryRaceDeterminatorImpl}'s constructor in order to
     * determine anniversary numbers based on the {@link AnniversaryChecker#update(int) provided race count}.
     */
    public AnniversaryRaceDeterminatorImpl(RacingEventService racingEventService,
            RemoteSailingServerSet remoteSailingServerSet, AnniversaryChecker... checkerToUse) {
        this.racingEventService = racingEventService;
        this.remoteSailingServerSet = remoteSailingServerSet;
        this.knownAnniversaries = new ConcurrentHashMap<>();
        this.isStarted = new AtomicBoolean(false);

        try {
            knownAnniversaries.putAll(racingEventService.getDomainObjectFactory().getAnniversaryData());
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

    void update() {
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
            allRaces.putAll(racingEventService.getLocalRaceList());
            if (allRaces.size() != currentRaceCount) {
                checkForNewAnniversaries(allRaces);
            }
        }
    }

    /**
     * Determines and stores anniversary that weren't found before. For every newly found anniversary, the
     * {@link DetailedRaceInfo detail information} need to be retrieved from the server where the race is hosted.
     */
    private void checkForNewAnniversaries(Map<RegattaAndRaceIdentifier, SimpleRaceInfo> races) {
        if (isStarted.get()) {
            final ArrayList<SimpleRaceInfo> allRaces = new ArrayList<>(races.values());
            final TimePoint now = MillisecondsTimePoint.now();
            allRaces.removeIf(race -> now.before(race.getStartOfRace()));
            Collections.sort(allRaces, new Comparator<SimpleRaceInfo>() {
                @Override
                public int compare(SimpleRaceInfo o1, SimpleRaceInfo o2) {
                    return o1.getStartOfRace().compareTo(o2.getStartOfRace());
                }
            });
            
            final Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversariesToAdd = new HashMap<>();
            Integer nearestNext = null;
            AnniversaryType nearestType = null;
            for (AnniversaryChecker checker : checkers) {
                checker.update(allRaces.size());
                // find past anniversaries
                for (Integer anniversary : checker.getAnniversaries()) {
                    if (!knownAnniversaries.containsKey(anniversary)) {
                        final Pair<DetailedRaceInfo, AnniversaryType> anniversaryData = resolveAnniversaryData(
                                anniversary, allRaces.get(anniversary - 1), checker.getType());
                        if (anniversaryData != null) {
                            anniversariesToAdd.put(anniversary, anniversaryData);
                        }
                    }
                }
                // find next anniversaries
                Integer next = checker.getNextAnniversary();
                if (next != null && (nearestNext == null || next < nearestNext)) {
                    nearestNext = next;
                    nearestType = checker.getType();
                }
            }
            
            synchronized (this) {
                if (nearestNext != null && (nextAnniversary == null || nearestNext > nextAnniversary.getA())) {
                    racingEventService.apply(new UpdateNextAnniversaryOperation(
                            new Pair<Integer, AnniversaryType>(nearestNext, nearestType)));
                }
                
                boolean requiresPersist = false;
                for (Map.Entry<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversaryEntry : anniversariesToAdd
                        .entrySet()) {
                    final Integer anniversary = anniversaryEntry.getKey();
                    if (!knownAnniversaries.containsKey(anniversary)) {
                        racingEventService.apply(new AddAnniversaryOperation(anniversary, anniversaryEntry.getValue()));
                        requiresPersist = true;
                    }
                }
                racingEventService.apply(new UpdateRaceCountOperation(allRaces.size()));
                if (requiresPersist) {
                    racingEventService.getMongoObjectFactory().storeAnniversaryData(knownAnniversaries);
                }
            }
        }
    }
    
    /**
     * Retrieves the detail data for a newly identified anniversary race.
     */
    private Pair<DetailedRaceInfo, AnniversaryType> resolveAnniversaryData(final Integer anniversary, final SimpleRaceInfo simpleRaceInfo, final AnniversaryType anniversaryType) {
        final DetailedRaceInfo fullData = racingEventService.getFullDetailsForRaceCascading(simpleRaceInfo.getIdentifier());
        logger.info("Determined new Anniversary! " + anniversary +" - " + simpleRaceInfo + " - " + anniversaryType + " - " + fullData);
        if (fullData == null) {
            logger.severe("Detailed data for anniversary " + anniversary + " - " + simpleRaceInfo + " could not be resolved");
            return null;
        }
        return new Pair<>(fullData, anniversaryType);
    }

    @Override
    public synchronized void addAnniversary(int anniversaryToCheck, final Pair<DetailedRaceInfo, AnniversaryType> anniversaryData) {
        knownAnniversaries.put(anniversaryToCheck, anniversaryData);
    }
    
    @Override
    public synchronized void setNextAnniversary(Pair<Integer, AnniversaryType> nextAnniversary) {
        this.nextAnniversary = nextAnniversary;
    }
    
    @Override
    public synchronized void setRaceCount(int raceCount) {
        currentRaceCount = raceCount;
    }

    @Override
    public Pair<Integer, AnniversaryType> getNextAnniversary() {
        return nextAnniversary;
    }

    @Override
    public Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> getKnownAnniversaries() {
        return new HashMap<>(knownAnniversaries);
    }
    
    @Override
    public synchronized void setKnownAnniversaries(Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversaries) {
        knownAnniversaries.clear();
        if (anniversaries != null) {
            knownAnniversaries.putAll(anniversaries);
        }
    }

    @Override
    public int getCurrentRaceCount() {
        return currentRaceCount;
    }
    
    @Override
    public void start() {
        isStarted.set(true);
        remoteSailingServerSet.addRemoteRaceResultReceivedCallback(raceChangedListener);
    }
    
    @Override
    public synchronized void clearAndStop() {
        isStarted.set(false);
        remoteSailingServerSet.removeRemoteRaceResultReceivedCallback(raceChangedListener);
        clear();
    }

    @Override
    public synchronized void clear() {
        knownAnniversaries.clear();
        nextAnniversary = null;
        currentRaceCount = 0;
    }
}
