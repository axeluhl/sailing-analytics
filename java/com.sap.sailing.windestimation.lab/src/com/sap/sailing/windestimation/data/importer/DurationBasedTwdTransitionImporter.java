package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
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

public class DurationBasedTwdTransitionImporter {

    private static final double SECONDS_TO_AGGREGATE = 10;
    private static final double SECONDS_PASSED_FACTOR_FOR_AGGREGATION = 0.1;

    public static void main(String[] args) throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import started");
        RaceWithWindSourcesPersistenceManager racesPersistenceManager = new RaceWithWindSourcesPersistenceManager();
        SingleDimensionBasedTwdTransitionPersistenceManager durationBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DURATION);
        durationBasedTwdTransitionPersistenceManager.dropCollection();
        for (PersistedElementsIterator<RaceWithWindSources> iterator = racesPersistenceManager
                .getIterator(null); iterator.hasNext();) {
            RaceWithWindSources race = iterator.next();
            LoggingUtil.logInfo("Processing race " + race.getRaceName() + " of regatta " + race.getRegattaName());
            List<SingleDimensionBasedTwdTransition> entries = new ArrayList<>();
            TimePoint timePointOfLastConsideredWindFix = null;
            for (WindSourceWithFixes windSource : race.getWindSources()) {
                int windFixIndex = 0;
                for (Wind windFix : windSource.getWindFixes()) {
                    if (timePointOfLastConsideredWindFix == null || timePointOfLastConsideredWindFix
                            .until(windFix.getTimePoint()).asSeconds() >= SECONDS_TO_AGGREGATE) {
                        timePointOfLastConsideredWindFix = windFix.getTimePoint();
                        TimePoint timePointOfLastConsideredOtherWindFix = timePointOfLastConsideredWindFix;
                        for (ListIterator<Wind> otherWindFixesIterator = windSource.getWindFixes()
                                .listIterator(++windFixIndex); otherWindFixesIterator.hasNext();) {
                            Wind otherWindFix = otherWindFixesIterator.next();
                            double secondsPassedSinceLastConsideredWindFix = timePointOfLastConsideredOtherWindFix
                                    .until(otherWindFix.getTimePoint()).asSeconds();
                            double secondsPassed = windFix.getTimePoint().until(otherWindFix.getTimePoint())
                                    .asSeconds();
                            if (secondsPassedSinceLastConsideredWindFix >= SECONDS_TO_AGGREGATE
                                    + secondsPassed * SECONDS_PASSED_FACTOR_FOR_AGGREGATION) {
                                double absTwdChange = windFix.getBearing().getDifferenceTo(otherWindFix.getBearing())
                                        .abs().getDegrees();
                                SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(
                                        secondsPassed, absTwdChange);
                                entries.add(entry);
                                timePointOfLastConsideredOtherWindFix = otherWindFix.getTimePoint();
                            }
                        }
                    }
                }
                if (entries.isEmpty()) {
                    LoggingUtil.logInfo("No TWD transitions to import");
                } else {
                    durationBasedTwdTransitionPersistenceManager.add(entries);
                    LoggingUtil.logInfo(entries.size() + " TWD transition entries imported");
                }
            }
        }
        long valuesCount = 0;
        double twdSum = 0.0;
        double squareTwdSum = 0.0;
        double secondsPassed = 0;
        double secondsPassedAtLastAggregation = 0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregates = new ArrayList<>();
        LoggingUtil.logInfo("Aggregating persisted entries");
        for (PersistedElementsIterator<SingleDimensionBasedTwdTransition> iterator = durationBasedTwdTransitionPersistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            SingleDimensionBasedTwdTransition entry = iterator.next();
            double newSecondsPassed = entry.getDimensionValue();
            if (newSecondsPassed - secondsPassedAtLastAggregation >= SECONDS_TO_AGGREGATE) {
                AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum,
                        squareTwdSum, secondsPassed);
                aggregates.add(aggregate);
                secondsPassedAtLastAggregation = secondsPassed;
            }
            secondsPassed = newSecondsPassed;
            double twdChange = entry.getAbsTwdChangeInDegrees();
            twdSum += twdChange;
            squareTwdSum += twdChange * twdChange;
            valuesCount++;
            if (valuesCount % 10000 == 0) {
                LoggingUtil.logInfo(valuesCount + " Entries aggregated");
            }
        }
        if (secondsPassedAtLastAggregation < secondsPassed) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum, squareTwdSum,
                    secondsPassed);
            aggregates.add(aggregate);
        }
        LoggingUtil.logInfo("Persisting " + aggregates.size() + " aggregates");
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager aggregatedDurationBasedTwdTransitionPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION);
        aggregatedDurationBasedTwdTransitionPersistenceManager.dropCollection();
        aggregatedDurationBasedTwdTransitionPersistenceManager.add(aggregates);
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
