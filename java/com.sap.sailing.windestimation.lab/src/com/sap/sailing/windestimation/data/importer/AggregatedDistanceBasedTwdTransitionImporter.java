package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;

public class AggregatedDistanceBasedTwdTransitionImporter {

    public static void main(String[] args) throws UnknownHostException {
        SingleDimensionBasedTwdTransitionPersistenceManager singleDimensionBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DISTANCE);
        SingleDimensionTwdTransitionAggregationImporter importer = new SingleDimensionTwdTransitionAggregationImporter(
                singleDimensionBasedTwdTransitionPersistenceManager, AggregatedSingleDimensionType.DISTANCE, 10, null);
        importer.runAggregation();
    }

}
