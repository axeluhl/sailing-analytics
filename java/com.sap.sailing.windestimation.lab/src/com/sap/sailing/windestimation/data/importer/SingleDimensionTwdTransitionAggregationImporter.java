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

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import smile.math.Math;
import smile.sort.QuickSelect;

public class SingleDimensionTwdTransitionAggregationImporter {

    private static final int MIN_NUMBER_OF_VALUES_PER_BUCKET = 10000;
    private final SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionTwdTransitionPersistenceManager;
    private final AggregatedSingleDimensionType dimensionType;
    private final NextThresholdCalculator thresholdCalculator;

    public SingleDimensionTwdTransitionAggregationImporter(
            SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionTwdTransitionPersistenceManager,
            AggregatedSingleDimensionType dimensionType, NextThresholdCalculator thresholdCalculator) {
        this.singleDimensionTwdTransitionPersistenceManager = singleDimensionTwdTransitionPersistenceManager;
        this.dimensionType = dimensionType;
        this.thresholdCalculator = thresholdCalculator;
    }

    public void runAggregation() throws UnknownHostException {
        long totalValuesCount = 0;
        List<AggregatedSingleDimensionBasedTwdTransition> aggregates = new ArrayList<>();
        LoggingUtil.logInfo("Aggregating persisted entries");
        TDoubleList entries = new TDoubleArrayList();
        double currentBucketThreshold = thresholdCalculator.getInitialThresholdValue();
        double nextBucketThreshold = thresholdCalculator.getNextThresholdValue(currentBucketThreshold);
        double finalBucketThreshold = getFinalBucketThreshold(currentBucketThreshold, nextBucketThreshold);
        for (PersistedElementsIterator<SingleDimensionBasedTwdTransition> iterator = singleDimensionTwdTransitionPersistenceManager
                .getIteratorSorted(); iterator.hasNext();) {
            SingleDimensionBasedTwdTransition entry = iterator.next();
            while (entry.getDimensionValue() > finalBucketThreshold) {
                if (entries.size() >= MIN_NUMBER_OF_VALUES_PER_BUCKET) {
                    AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(currentBucketThreshold,
                            entries);
                    aggregates.add(aggregate);
                }
                currentBucketThreshold = nextBucketThreshold;
                nextBucketThreshold = thresholdCalculator.getNextThresholdValue(nextBucketThreshold);
                finalBucketThreshold = getFinalBucketThreshold(currentBucketThreshold, nextBucketThreshold);
                totalValuesCount += entries.size();
                entries.clear();
            }
            double twdChange = entry.getTwdChangeInDegrees();
            if (dimensionType.isAbsolute()) {
                twdChange = Math.abs(twdChange);
            }
            entries.add(twdChange);
            if (entries.size() % 10000 == 0) {
                LoggingUtil.logInfo((entries.size() + totalValuesCount) + " Entries aggregated");
            }
        }
        if (entries.size() >= MIN_NUMBER_OF_VALUES_PER_BUCKET) {
            AggregatedSingleDimensionBasedTwdTransition aggregate = computeAggregate(currentBucketThreshold, entries);
            aggregates.add(aggregate);
            totalValuesCount += entries.size();
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

    private double getFinalBucketThreshold(double currentBucketThreshold, double nextBucketThreshold) {
        return currentBucketThreshold + (nextBucketThreshold - currentBucketThreshold) / 2;
    }

    private static AggregatedSingleDimensionBasedTwdTransition computeAggregate(double dimensionValue,
            TDoubleList entries) {
        double[] twdChanges = entries.toArray();
        double std;
        double zeroMeanStd;
        double median;
        double mean;
        double q1;
        double q3;
        double p1;
        double p99;
        if (entries.size() > 1) {
            std = Math.sd(twdChanges);
            double sqSum = 0;
            for (double twdChange : twdChanges) {
                sqSum += twdChange * twdChange;
            }
            zeroMeanStd = Math.sqrt(sqSum / (twdChanges.length - 1));
            median = QuickSelect.median(twdChanges);
            mean = Math.mean(twdChanges);
            q1 = QuickSelect.q1(twdChanges);
            q3 = QuickSelect.q3(twdChanges);
            p1 = QuickSelect.select(twdChanges, (int) (twdChanges.length * 0.01));
            p99 = QuickSelect.select(twdChanges, (int) (twdChanges.length * 0.99));
        } else {
            std = 0;
            zeroMeanStd = 0;
            median = entries.get(0);
            mean = median;
            q1 = median;
            q3 = median;
            p1 = median;
            p99 = median;
        }
        AggregatedSingleDimensionBasedTwdTransition aggregate = new AggregatedSingleDimensionBasedTwdTransition(
                dimensionValue, mean, std, zeroMeanStd, median, twdChanges.length, q1, q3, p1, p99);
        return aggregate;
    }

}
