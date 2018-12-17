package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
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
                        Wind previousOtherFix = otherFixesIterator.next();
                        Wind previousFix = fixesIterator.next();
                        Wind currentFix = null;
                        Wind currentOtherFix = null;
                        TimePoint timePointOfLastTransition = null;
                        double bestDuration = previousFix.getTimePoint().until(previousOtherFix.getTimePoint()).abs()
                                .asSeconds();
                        do {
                            while (fixesIterator.hasNext()) {
                                currentFix = fixesIterator.next();
                                double duration = previousOtherFix.getTimePoint().until(currentFix.getTimePoint()).abs()
                                        .asSeconds();
                                if (bestDuration > duration) {
                                    bestDuration = duration;
                                    previousFix = currentFix;
                                } else {
                                    break;
                                }
                            }
                            while (otherFixesIterator.hasNext()) {
                                currentOtherFix = otherFixesIterator.next();
                                double duration = previousFix.getTimePoint().until(currentOtherFix.getTimePoint()).abs()
                                        .asSeconds();
                                if (bestDuration > duration) {
                                    bestDuration = duration;
                                    previousOtherFix = currentOtherFix;
                                } else {
                                    break;
                                }
                            }
                            if (bestDuration <= TOLERANCE_SECONDS) {
                                TimePoint timePointOfNewTransition = previousFix.getTimePoint()
                                        .before(previousOtherFix.getTimePoint()) ? previousFix.getTimePoint()
                                                : previousOtherFix.getTimePoint();
                                if (timePointOfLastTransition == null || timePointOfLastTransition
                                        .until(timePointOfNewTransition).asSeconds() >= SAMPLING_SECONDS) {
                                    timePointOfLastTransition = previousFix.getTimePoint()
                                            .before(previousOtherFix.getTimePoint()) ? previousOtherFix.getTimePoint()
                                                    : previousFix.getTimePoint();
                                    double meters = previousFix.getPosition()
                                            .getDistance(previousOtherFix.getPosition()).getMeters();
                                    double twdChange = previousFix.getBearing()
                                            .getDifferenceTo(previousOtherFix.getBearing()).abs().getDegrees();
                                    SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(
                                            meters, twdChange);
                                    entries.add(entry);
                                }
                            }
                            previousFix = fixesIterator.hasNext() ? currentFix : null;
                            previousOtherFix = otherFixesIterator.hasNext() ? currentOtherFix : null;
                        } while (previousFix != null && previousOtherFix != null);
                    }
                }
            }
            if (entries.isEmpty()) {
                LoggingUtil.logInfo("No TWD transitions to import");
            } else {
                distanceBasedTwdTransitionPersistenceManager.add(entries);
                LoggingUtil.logInfo(entries.size() + " TWD transitions imported");
            }
        }
        long valuesCount = 0;
        double twdSum = 0.0;
        double squareTwdSum = 0.0;
        double meters = 0;
        double metersAtLastAggregation = 0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregates = new ArrayList<>();
        LoggingUtil.logInfo("Aggregating persisted entries");
        for (PersistedElementsIterator<SingleDimensionBasedTwdTransition> iterator = distanceBasedTwdTransitionPersistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            SingleDimensionBasedTwdTransition entry = iterator.next();
            double newMeters = entry.getDimensionValue();
            if (newMeters - metersAtLastAggregation >= METERS_TO_AGGREGATE) {
                AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum,
                        squareTwdSum, meters);
                aggregates.add(aggregate);
                metersAtLastAggregation = meters;
            }
            meters = newMeters;
            double twdChange = entry.getAbsTwdChangeInDegrees();
            twdSum += twdChange;
            squareTwdSum += twdChange * twdChange;
            valuesCount++;
            if (valuesCount % 10000 == 0) {
                LoggingUtil.logInfo(valuesCount + " Entries aggregated");
            }
        }
        if (metersAtLastAggregation < meters) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum, squareTwdSum,
                    meters);
            aggregates.add(aggregate);
        }
        LoggingUtil.logInfo("Persisting " + aggregates.size() + " aggregates");
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager aggregatedDistanceBasedTwdTransitionPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DISTANCE);
        aggregatedDistanceBasedTwdTransitionPersistenceManager.dropCollection();
        aggregatedDistanceBasedTwdTransitionPersistenceManager.add(aggregates);
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + valuesCount + " TWD transitions imported");
        LoggingUtil.logInfo("Totally " + aggregates.size() + " TWD transition aggregates imported");
    }

    private static AggregatedSingleDimensionBasedTwdTransition computeAggregate(double valuesCount, double twdSum,
            double squareTwdSum, double secondsPassed) {
        double mean = twdSum / valuesCount;
        double variance = (valuesCount * squareTwdSum - twdSum * twdSum) / (valuesCount * valuesCount);
        double std = Math.sqrt(variance);
        AggregatedSingleDimensionBasedTwdTransition aggregate = new AggregatedSingleDimensionBasedTwdTransition(
                secondsPassed, mean, std);
        return aggregate;
    }

}
