package com.sap.sailing.windestimation.datavisualization;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;

public class AggregatedRandomDimensionPlot {

    public static void main(String[] args) throws UnknownHostException {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION) {
            @Override
            public PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> getIteratorSorted() {
                return new PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition>() {

                    private long i = 0;
                    private long limit = 100;

                    @Override
                    public AggregatedSingleDimensionBasedTwdTransition next() {
                        if (hasNext()) {
                            return new AggregatedSingleDimensionBasedTwdTransition(i++, 30, 10, 20, 100000, 15, 35, 1,
                                    100);
                        }
                        return null;
                    }

                    @Override
                    public boolean hasNext() {
                        return i < limit;
                    }

                    @Override
                    public PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> limit(long limit) {
                        this.limit = limit;
                        return this;
                    }

                    @Override
                    public long getNumberOfElements() {
                        return limit;
                    }
                };
            }
        };
        SingleDimensionAggregatesPlottingFrame plot = new SingleDimensionAggregatesPlottingFrame(persistenceManager);
        plot.renderChart();
    }

}
