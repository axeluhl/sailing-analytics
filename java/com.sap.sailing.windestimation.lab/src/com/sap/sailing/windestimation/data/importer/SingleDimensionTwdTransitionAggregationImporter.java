package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;

import smile.sort.QuickSelect;

public class SingleDimensionTwdTransitionAggregationImporter {

    private final SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionTwdTransitionPersistenceManager;
    private final AggregatedSingleDimensionType dimensionType;
    private final double valueIntervalToAggregate;

    public SingleDimensionTwdTransitionAggregationImporter(
            SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionTwdTransitionPersistenceManager,
            AggregatedSingleDimensionType dimensionType, double valueIntervalToAggregate) {
        this.singleDimensionTwdTransitionPersistenceManager = singleDimensionTwdTransitionPersistenceManager;
        this.dimensionType = dimensionType;
        this.valueIntervalToAggregate = valueIntervalToAggregate;
    }

    public void runAggregation() throws UnknownHostException {
        long totalValuesCount = 0;
        long valuesCount = 0;
        double twdSum = 0.0;
        double squareTwdSum = 0.0;
        double valueDifference = 0;
        double valueDifferenceAtLastAggregation = 0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregates = new ArrayList<>();
        List<Double> entries = new ArrayList<>();
        LoggingUtil.logInfo("Aggregating persisted entries");
        for (PersistedElementsIterator<SingleDimensionBasedTwdTransition> iterator = singleDimensionTwdTransitionPersistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            SingleDimensionBasedTwdTransition entry = iterator.next();
            double newSecondsPassed = entry.getDimensionValue();
            if (valueDifference > 0
                    && newSecondsPassed - valueDifferenceAtLastAggregation >= valueIntervalToAggregate) {
                AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum,
                        squareTwdSum, valueDifference, entries);
                aggregates.add(aggregate);
                valueDifferenceAtLastAggregation = valueDifference;
                totalValuesCount += valuesCount;
                twdSum = 0;
                squareTwdSum = 0;
                valuesCount = 0;
                entries.clear();
            }
            valueDifference = newSecondsPassed;
            double twdChange = entry.getAbsTwdChangeInDegrees();
            twdSum += twdChange;
            squareTwdSum += twdChange * twdChange;
            valuesCount++;
            entries.add(entry.getAbsTwdChangeInDegrees());
            if (valuesCount % 10000 == 0) {
                LoggingUtil.logInfo((valuesCount + totalValuesCount) + " Entries aggregated");
            }
        }
        if (valueDifferenceAtLastAggregation < valueDifference) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum, squareTwdSum,
                    valueDifference, entries);
            aggregates.add(aggregate);
            totalValuesCount += valuesCount;
        }
        LoggingUtil.logInfo("Persisting " + aggregates.size() + " aggregates");
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager aggregatedDurationBasedTwdTransitionPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                dimensionType);
        aggregatedDurationBasedTwdTransitionPersistenceManager.dropCollection();
        aggregatedDurationBasedTwdTransitionPersistenceManager.add(aggregates);
        LoggingUtil.logInfo("###################\r\n" + dimensionType + " based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + totalValuesCount + " TWD transitions imported");
        LoggingUtil.logInfo("Totally " + aggregates.size() + " TWD transition aggregates imported");
    }

    private static AggregatedSingleDimensionBasedTwdTransition computeAggregate(double valuesCount, double twdSum,
            double squareTwdSum, double secondsPassed, List<Double> entries) {
        double mean = twdSum / valuesCount;
        double variance = (valuesCount * squareTwdSum - twdSum * twdSum) / (valuesCount * valuesCount);
        double std = Math.sqrt(variance);
        double[] twdChanges = new double[entries.size()];
        int i = 0;
        for (Double twdChange : entries) {
            twdChanges[i++] = twdChange;
        }
        double median = QuickSelect.median(twdChanges);
        double q1 = QuickSelect.q1(twdChanges);
        double q3 = QuickSelect.q3(twdChanges);
        double p1 = QuickSelect.select(twdChanges, (int) (twdChanges.length * 0.01));
        double p99 = QuickSelect.select(twdChanges, (int) (twdChanges.length * 0.99));

        AggregatedSingleDimensionBasedTwdTransition aggregate = new AggregatedSingleDimensionBasedTwdTransition(
                secondsPassed, mean, std, median, i, q1, q3, p1, p99);
        return aggregate;
    }

}
