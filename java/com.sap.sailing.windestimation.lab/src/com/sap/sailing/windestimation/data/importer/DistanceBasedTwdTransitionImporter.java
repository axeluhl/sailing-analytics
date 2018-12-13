package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
            Set<WindSourcesRelation> processedRelations = new HashSet<>();
            for (WindSourceWithFixes windSource : windSources) {
                for (WindSourceWithFixes otherWindSource : windSources) {
                    if (windSource != otherWindSource
                            && processedRelations.add(new WindSourcesRelation(windSource, otherWindSource))) {
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
                                        .until(timePointOfNewTransition).asSeconds() > TOLERANCE_SECONDS) {
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
                            previousFix = currentFix;
                            previousOtherFix = currentOtherFix;
                        } while (previousFix != null && previousOtherFix != null);
                    }
                }
            }
            distanceBasedTwdTransitionPersistenceManager.add(entries);
            LoggingUtil.logInfo(entries.size() + " TWD transitions imported");
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

    private static class WindSourcesRelation {
        private final WindSourceWithFixes a;
        private final WindSourceWithFixes b;

        public WindSourcesRelation(WindSourceWithFixes a, WindSourceWithFixes b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            return a.hashCode() + b.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            WindSourcesRelation other = (WindSourcesRelation) obj;
            if (a == other.a && b == other.b) {
                return true;
            }
            if (a == other.b && b == other.a) {
                return true;
            }
            return false;
        }

    }

}
