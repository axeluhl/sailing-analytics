package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class DurationBasedTwdTransitionImporter {

    public static final int SECONDS_TO_AGGREGATE = 10;

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
            for (WindSourceWithFixes windSource : race.getWindSources()) {
                if (windSource.getWindFixes().size() > 1) {
                    Iterator<Wind> windIterator = windSource.getWindFixes().iterator();
                    Wind previousWind = windIterator.next();
                    for (Wind currentWind = windIterator.next(); windIterator.hasNext();) {
                        double secondsPassedToPrevious = previousWind.getTimePoint().until(currentWind.getTimePoint())
                                .asSeconds();
                        double absTwdChange = previousWind.getBearing().getDifferenceTo(currentWind.getBearing()).abs()
                                .getDegrees();
                        SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(
                                secondsPassedToPrevious, absTwdChange);
                        entries.add(entry);
                        previousWind = currentWind;
                    }
                    durationBasedTwdTransitionPersistenceManager.add(entries);
                    LoggingUtil.logInfo(entries.size() + " TWD transition entries imported");
                } else {
                    LoggingUtil.logInfo("No TWD transitions to import");
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
