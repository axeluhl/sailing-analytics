package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;

public class AggregatedDurationBasedAbsTwdTransitionImporter {

    public static void main(String[] args) throws UnknownHostException {
        SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DURATION);
        SingleDimensionTwdTransitionAggregationImporter importer = new SingleDimensionTwdTransitionAggregationImporter(
                singleDimensionBasedTwdTransitionPersistenceManager, AggregatedSingleDimensionType.DURATION_ABS,
                DurationBasedTwdTransitionImporter.SECONDS_INTERVAL_TO_SAMPLE,
                DurationBasedTwdTransitionImporter.ANNEALING_FACTOR_FOR_SECONDS_PASSED_FOR_SAMPLING);
        importer.runAggregation();
    }

}
