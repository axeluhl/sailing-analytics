package com.sap.sailing.windestimation.datavisualization;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;

public class AggregatedDistanceDimensionPlot {

    private static SingleDimensionAggregatesPlottingFrame plot;

    public static void main(String[] args) throws UnknownHostException {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DISTANCE);
        plot = new SingleDimensionAggregatesPlottingFrame(persistenceManager);
        plot.renderChart();
    }

    public static void awaitWindowClosed() throws InterruptedException {
        plot.awaitWindowClosed();
    }

}
