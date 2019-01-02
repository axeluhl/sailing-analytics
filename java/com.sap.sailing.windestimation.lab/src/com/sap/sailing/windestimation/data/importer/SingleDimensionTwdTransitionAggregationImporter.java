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

import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import smile.sort.QuickSelect;

public class SingleDimensionTwdTransitionAggregationImporter {

    private final SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionTwdTransitionPersistenceManager;
    private final AggregatedSingleDimensionType dimensionType;
    private final double valueIntervalToAggregate;
    private double annealingFactor;

    public SingleDimensionTwdTransitionAggregationImporter(
            SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionTwdTransitionPersistenceManager,
            AggregatedSingleDimensionType dimensionType, double valueIntervalToAggregate, Double annealingFactor) {
        this.singleDimensionTwdTransitionPersistenceManager = singleDimensionTwdTransitionPersistenceManager;
        this.dimensionType = dimensionType;
        this.valueIntervalToAggregate = valueIntervalToAggregate;
        this.annealingFactor = annealingFactor == null ? 0.0 : annealingFactor;
    }

    public void runAggregation() throws UnknownHostException {
        long totalValuesCount = 0;
        long valuesCount = 0;
        double twdSum = 0.0;
        double squareTwdSum = 0.0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregates = new ArrayList<>();
        LoggingUtil.logInfo("Aggregating persisted entries");
        TFloatList entries = new TFloatArrayList();
        double currentBucketThreshold = valueIntervalToAggregate;
        double nextBucketThreshold = getNextBucketThreshold(currentBucketThreshold);
        double finalBucketThreshold = getFinalBucketThreshold(currentBucketThreshold, nextBucketThreshold);
        for (PersistedElementsIterator<SingleDimensionBasedTwdTransition> iterator = singleDimensionTwdTransitionPersistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            SingleDimensionBasedTwdTransition entry = iterator.next();
            while (entry.getDimensionValue() > finalBucketThreshold) {
                if (entries.size() >= 100) {
                    AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum,
                            squareTwdSum, currentBucketThreshold, entries);
                    aggregates.add(aggregate);
                }
                currentBucketThreshold = nextBucketThreshold;
                nextBucketThreshold = getNextBucketThreshold(nextBucketThreshold);
                finalBucketThreshold = getFinalBucketThreshold(currentBucketThreshold, nextBucketThreshold);
                totalValuesCount += valuesCount;
                twdSum = 0;
                squareTwdSum = 0;
                valuesCount = 0;
                entries.clear();
            }
            double twdChange = entry.getAbsTwdChangeInDegrees();
            twdSum += twdChange;
            squareTwdSum += twdChange * twdChange;
            valuesCount++;
            entries.add((float) twdChange);
            if (valuesCount % 10000 == 0) {
                LoggingUtil.logInfo((valuesCount + totalValuesCount) + " Entries aggregated");
            }
        }
        if (entries.size() >= 100) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(valuesCount, twdSum, squareTwdSum,
                    currentBucketThreshold, entries);
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

    private double getNextBucketThreshold(double currentBucketThreshold) {
        if(annealingFactor > 0) {
            return currentBucketThreshold * annealingFactor;
        }
        return currentBucketThreshold + valueIntervalToAggregate;
    }

    private double getFinalBucketThreshold(double currentBucketThreshold, double nextBucketThreshold) {
        return currentBucketThreshold + (nextBucketThreshold - currentBucketThreshold) / 2;
    }

    private static AggregatedSingleDimensionBasedTwdTransition computeAggregate(long valuesCount, double twdSum,
            double squareTwdSum, double secondsPassed, TFloatList entries) {
        double mean = twdSum / valuesCount;
        double variance = (valuesCount * squareTwdSum - twdSum * twdSum) / (valuesCount * valuesCount);
        double std = Math.sqrt(variance);
        float[] twdChanges = entries.toArray();
        double median;
        double q1;
        double q3;
        double p1;
        double p99;
        if (entries.size() > 1) {
            median = QuickSelect.median(twdChanges);
            q1 = QuickSelect.q1(twdChanges);
            q3 = QuickSelect.q3(twdChanges);
            p1 = QuickSelect.select(twdChanges, (int) (twdChanges.length * 0.01));
            p99 = QuickSelect.select(twdChanges, (int) (twdChanges.length * 0.99));
        } else {
            median = entries.get(0);
            q1 = median;
            q3 = median;
            p1 = median;
            p99 = median;
        }
        AggregatedSingleDimensionBasedTwdTransition aggregate = new AggregatedSingleDimensionBasedTwdTransition(
                secondsPassed, mean, std, median, twdChanges.length, q1, q3, p1, p99);
        return aggregate;
    }

}
