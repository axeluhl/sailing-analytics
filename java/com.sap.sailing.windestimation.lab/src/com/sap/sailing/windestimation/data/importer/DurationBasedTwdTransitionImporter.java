package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.RaceWithWindSourcesPersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class DurationBasedTwdTransitionImporter {

    public static final int SECONDS_TO_AGGREGATE = 10;

    public static void main(String[] args) throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import started");
        RaceWithWindSourcesPersistenceManager racesPersistenceManager = new RaceWithWindSourcesPersistenceManager();
        long aggregatesCount = 0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregates = new ArrayList<>();
        for (PersistedElementsIterator<RaceWithWindSources> iterator = racesPersistenceManager
                .getIterator(null); iterator.hasNext();) {
            RaceWithWindSources race = iterator.next();
            LoggingUtil.logInfo("Processing race " + race.getRaceName() + " of regatta " + race.getRegattaName());
            for (WindSourceWithFixes windSource : race.getWindSources()) {
                double recordingSeconds = windSource.getWindSourceMetadata().getStartTime()
                        .until(windSource.getWindSourceMetadata().getEndTime()).asSeconds();
                int numberOfAggregates = (int) Math.ceil(recordingSeconds / 10);
                if (numberOfAggregates > 0) {
                    Iterator<Wind> windIterator = windSource.getWindFixes().iterator();
                    double valuesCount = 0;
                    double twdSum = 0.0;
                    double squareTwdSum = 0.0;
                    Wind previousWind = windIterator.next();
                    double secondsPassed = 0;
                    double secondsPassedAtLastAggregation = 0;
                    for (Wind currentWind = windIterator.next(); windIterator.hasNext();) {
                        double newSecondsPassed = secondsPassed
                                + previousWind.getTimePoint().until(currentWind.getTimePoint()).asSeconds();
                        if (newSecondsPassed - secondsPassedAtLastAggregation >= SECONDS_TO_AGGREGATE
                                && newSecondsPassed > 0) {
                            AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount,
                                    twdSum, squareTwdSum, secondsPassed);
                            aggregates.add(aggregate);
                            secondsPassedAtLastAggregation = secondsPassed;
                        }
                        secondsPassed = newSecondsPassed;
                        valuesCount++;
                        double absTwdChange = previousWind.getBearing().getDifferenceTo(currentWind.getBearing()).abs()
                                .getDegrees();
                        twdSum += absTwdChange;
                        squareTwdSum += absTwdChange * absTwdChange;
                        previousWind = currentWind;
                    }
                    if (secondsPassedAtLastAggregation < secondsPassed) {
                        AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum,
                                squareTwdSum, secondsPassed);
                        aggregates.add(aggregate);
                    }
                    LoggingUtil.logInfo(numberOfAggregates + " TWD transition aggregates imported in-memory");
                } else {
                    LoggingUtil.logInfo("No TWD transition aggregates to import");
                }
            }
        }
        LoggingUtil.logInfo("Sorting aggregates in-memory");
        Collections.sort(aggregates, (one, two) -> Double.compare(one.getDimensionValue(), two.getDimensionValue()));
        LoggingUtil.logInfo("Persisting sorted aggregates");
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager durationBasedTwdTransitionPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION);
        durationBasedTwdTransitionPersistenceManager.dropCollection();
        durationBasedTwdTransitionPersistenceManager.add(aggregates);
        LoggingUtil.logInfo("Aggregating sorted aggregates");
        double valuesCount = 0;
        double meanSum = 0.0;
        double stdSum = 0.0;
        double secondsPassed = 0;
        double secondsPassedAtLastAggregation = 0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregatedAggregates = new ArrayList<>();
        for (AggregatedSingleDimensionBasedTwdTransition aggregate : aggregates) {
            double newSecondsPassed = aggregate.getDimensionValue();
            if (newSecondsPassed - secondsPassedAtLastAggregation >= SECONDS_TO_AGGREGATE) {
                AggregatedSingleDimensionBasedTwdTransition aggregatedAggregate = new AggregatedSingleDimensionBasedTwdTransition(
                        secondsPassed, meanSum / valuesCount, stdSum / valuesCount);
                aggregatedAggregates.add(aggregatedAggregate);
                secondsPassedAtLastAggregation = secondsPassed;
            }
            secondsPassed = newSecondsPassed;
            meanSum += aggregate.getMean();
            stdSum += aggregate.getStd();
            valuesCount++;
        }
        if (secondsPassedAtLastAggregation < secondsPassed) {
            AggregatedSingleDimensionBasedTwdTransition aggregatedAggregate = new AggregatedSingleDimensionBasedTwdTransition(
                    secondsPassed, meanSum / valuesCount, stdSum / valuesCount);
            aggregatedAggregates.add(aggregatedAggregate);
        }
        LoggingUtil.logInfo("Persisting aggregated aggregates");
        durationBasedTwdTransitionPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.AGGREGATED_DURATION);
        durationBasedTwdTransitionPersistenceManager.dropCollection();
        durationBasedTwdTransitionPersistenceManager.add(aggregatedAggregates);
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + aggregatesCount + " TWD transition aggregates imported");
        LoggingUtil.logInfo("Totally " + valuesCount + " TWD transition aggregated aggregates imported");
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
