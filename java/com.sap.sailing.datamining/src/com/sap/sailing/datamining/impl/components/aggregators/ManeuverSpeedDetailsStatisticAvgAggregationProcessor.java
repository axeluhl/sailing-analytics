package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;
import com.sap.sailing.datamining.impl.components.aggregators.ManeuverSpeedDetailsStatisticAggregationProcessorHelper.ManeuverSpeedDetailsAggregationCreator;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregationImpl;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.SimpleAggregationProcessorDefinition;
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsStatisticAvgAggregationProcessor extends
        AbstractParallelGroupedDataAggregationProcessor<ManeuverSpeedDetailsStatistic, ManeuverSpeedDetailsAggregation> {

    private static final String MESSAGE_KEY = "AvgTrendForTWAs";
    private final ManeuverSpeedDetailsStatisticAggregationProcessorHelper helper = new ManeuverSpeedDetailsStatisticAggregationProcessorHelper(
            AvgManeuverSpeedDetailsAggregationCreator.class);

    public ManeuverSpeedDetailsStatisticAvgAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, ManeuverSpeedDetailsAggregation>, ?>> resultReceivers) {
        super(executor, resultReceivers, MESSAGE_KEY);
    }

    private static final AggregationProcessorDefinition<ManeuverSpeedDetailsStatistic, ManeuverSpeedDetailsAggregation> DEFINITION = new SimpleAggregationProcessorDefinition<>(
            ManeuverSpeedDetailsStatistic.class, ManeuverSpeedDetailsAggregation.class, MESSAGE_KEY,
            ManeuverSpeedDetailsStatisticAvgAggregationProcessor.class);

    public static AggregationProcessorDefinition<ManeuverSpeedDetailsStatistic, ManeuverSpeedDetailsAggregation> getDefinition() {
        return DEFINITION;
    }

    @Override
    protected void handleElement(GroupedDataEntry<ManeuverSpeedDetailsStatistic> element) {
        helper.storeElement(element);
    }

    @Override
    protected Map<GroupKey, ManeuverSpeedDetailsAggregation> getResult() {
        return helper.aggregateResult();
    }

    static class AvgManeuverSpeedDetailsAggregationCreator implements ManeuverSpeedDetailsAggregationCreator {
        private double[] sumValuesPerTWA = new double[360];
        private int[] countPerTWA = new int[360];
        private int count = 0;

        public void addElement(ManeuverSpeedDetailsStatistic dataEntry) {
            double[] maneuverValuePerTWA = dataEntry.getManeuverValuePerTWA();
            for (int i = 0; i < maneuverValuePerTWA.length; ++i) {
                if (maneuverValuePerTWA[i] != 0) {
                    sumValuesPerTWA[i] += maneuverValuePerTWA[i];
                    ++countPerTWA[i];
                }
            }
            count++;
        }

        public ManeuverSpeedDetailsAggregationImpl aggregateResult() {
            double[] averages = new double[360];
            for (int i = 0; i < 360; i++) {
                if (countPerTWA[i] > 0) {
                    averages[i] = sumValuesPerTWA[i] / countPerTWA[i];
                }
            }
            return new ManeuverSpeedDetailsAggregationImpl(averages, countPerTWA, count);
        }
    }
}
