package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.RaceWithWindSourcesPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.TimePoint;

public class DistanceBasedTwdTransitionImporter {

    public static final int METERS_TO_AGGREGATE = 100;
    public static final int TOLERANCE_SECONDS = 5;
    public static final int SAMPLING_SECONDS = 60;
    public static final int MAX_DISTANCE_METERS = 1000000;
    public static final int MIN_DISTANCE_METERS = 10;

    public static void main(String[] args) throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nDistance based TWD transitions Import started");
        RaceWithWindSourcesPersistenceManager racesPersistenceManager = new RaceWithWindSourcesPersistenceManager();
        SingleDimensionBasedTwdTransitionPersistenceManager distanceBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DISTANCE);
        distanceBasedTwdTransitionPersistenceManager.dropCollection();
        long totalValuesCount = 0;
        for (PersistedElementsIterator<RaceWithWindSources> iterator = racesPersistenceManager
                .getIterator(null); iterator.hasNext();) {
            RaceWithWindSources race = iterator.next();
            LoggingUtil.logInfo("Processing race " + race.getRaceName() + " of regatta " + race.getRegattaName());
            List<SingleDimensionBasedTwdTransition> entries = new ArrayList<>();
            List<WindSourceWithFixes> windSources = new ArrayList<>();
            for (PersistedElementsIterator<RaceWithWindSources> neighborRacesIterator = racesPersistenceManager
                    .getIteratorForEntriesIntersectingPeriod(race.getWindSourceMetadata().getStartTime(),
                            race.getWindSourceMetadata().getEndTime(), TOLERANCE_SECONDS); neighborRacesIterator
                                    .hasNext();) {
                RaceWithWindSources neighbor = neighborRacesIterator.next();
                windSources.addAll(neighbor.getWindSources());
            }
            int windSourceIndex = 0;
            for (WindSourceWithFixes windSource : windSources) {
                for (ListIterator<WindSourceWithFixes> otherWindSources = windSources
                        .listIterator(++windSourceIndex); otherWindSources.hasNext();) {
                    WindSourceWithFixes otherWindSource = otherWindSources.next();
                    double distanceBetweenWindSourcesInMeters = windSource.getWindSourceMetadata().getFirstPosition()
                            .getDistance(otherWindSource.getWindSourceMetadata().getFirstPosition()).getMeters();
                    if (distanceBetweenWindSourcesInMeters <= MAX_DISTANCE_METERS
                            && distanceBetweenWindSourcesInMeters >= MIN_DISTANCE_METERS) {
                        Iterator<Wind> fixesIterator = windSource.getWindFixes().iterator();
                        Iterator<Wind> otherFixesIterator = otherWindSource.getWindFixes().iterator();
                        Wind currentOtherFix = otherFixesIterator.next();
                        Wind currentFix = fixesIterator.next();
                        Wind nextFix = null;
                        Wind nextOtherFix = null;
                        TimePoint timePointOfLastTransition = null;
                        double bestDuration = currentFix.getTimePoint().until(currentOtherFix.getTimePoint()).abs()
                                .asSeconds();
                        do {
                            while (fixesIterator.hasNext()) {
                                nextFix = fixesIterator.next();
                                double duration = currentOtherFix.getTimePoint().until(nextFix.getTimePoint()).abs()
                                        .asSeconds();
                                if (bestDuration > duration) {
                                    bestDuration = duration;
                                    currentFix = nextFix;
                                } else {
                                    break;
                                }
                            }
                            while (otherFixesIterator.hasNext()) {
                                nextOtherFix = otherFixesIterator.next();
                                double duration = currentFix.getTimePoint().until(nextOtherFix.getTimePoint()).abs()
                                        .asSeconds();
                                if (bestDuration > duration) {
                                    bestDuration = duration;
                                    currentOtherFix = nextOtherFix;
                                } else {
                                    break;
                                }
                            }
                            if (bestDuration <= TOLERANCE_SECONDS) {
                                TimePoint timePointOfNewTransition = currentFix.getTimePoint()
                                        .before(currentOtherFix.getTimePoint()) ? currentFix.getTimePoint()
                                                : currentOtherFix.getTimePoint();
                                if (timePointOfLastTransition == null || timePointOfLastTransition
                                        .until(timePointOfNewTransition).asSeconds() >= SAMPLING_SECONDS) {
                                    timePointOfLastTransition = currentFix.getTimePoint()
                                            .before(currentOtherFix.getTimePoint()) ? currentOtherFix.getTimePoint()
                                                    : currentFix.getTimePoint();
                                    double meters = currentFix.getPosition().getDistance(currentOtherFix.getPosition())
                                            .getMeters();
                                    double twdChange = currentFix.getBearing()
                                            .getDifferenceTo(currentOtherFix.getBearing()).abs().getDegrees();
                                    SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(
                                            meters, twdChange);
                                    entries.add(entry);
                                }
                            }
                            currentFix = fixesIterator.hasNext() ? nextFix : null;
                            currentOtherFix = otherFixesIterator.hasNext() ? nextOtherFix : null;
                        } while (currentFix != null && currentOtherFix != null);
                    }
                }
            }
            if (entries.isEmpty()) {
                LoggingUtil.logInfo("No TWD transitions to import");
            } else {
                distanceBasedTwdTransitionPersistenceManager.add(entries);
                int totalEntries = entries.size();
                totalValuesCount += totalEntries;
                LoggingUtil.logInfo(totalEntries + " TWD transitions imported, " + totalValuesCount + " in total");
            }
        }
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + totalValuesCount + " TWD transitions imported");
    }

}
