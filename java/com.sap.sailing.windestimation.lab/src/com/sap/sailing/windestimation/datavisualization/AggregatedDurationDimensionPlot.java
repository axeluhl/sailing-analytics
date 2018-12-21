package com.sap.sailing.windestimation.datavisualization;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;

public class AggregatedDurationDimensionPlot {

    public static void main(String[] args) throws UnknownHostException {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION);
        SingleDimensionAggregatesPlottingFrame plot = new SingleDimensionAggregatesPlottingFrame(persistenceManager);
        plot.renderChart();
    }

}
