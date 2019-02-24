package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;

public class AggregatedDurationBasedTwdTransitionImporter {

    public static void main(String[] args) throws UnknownHostException {
        SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionBasedTwdTransitionPersistenceManager = createPersistenceManagerAndEnsureIndex();
        SingleDimensionTwdTransitionAggregationImporter importer = new SingleDimensionTwdTransitionAggregationImporter(
                singleDimensionBasedTwdTransitionPersistenceManager, AggregatedSingleDimensionType.DURATION,
                DurationBasedTwdTransitionImporter.getThresholdCalculator());
        importer.runAggregation();
    }

    public static SingleDimensionBasedTwdTransitionPersistenceManager createPersistenceManagerAndEnsureIndex()
            throws UnknownHostException {
        SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DURATION);
        return singleDimensionBasedTwdTransitionPersistenceManager;
    }

}
