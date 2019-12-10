package com.sap.sailing.windestimation.datavisualization;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;

public class AggregatedDurationDimensionAbsPlot {

    public static void main(String[] args) throws UnknownHostException {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION_ABS);
        SingleDimensionAggregatesPlottingFrame plot = new SingleDimensionAggregatesPlottingFrame(persistenceManager);
        plot.renderChart();
    }

}
