package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.sap.sse.datamining.impl.components.aggregators.AbstractParallelGroupedDataStoringAggregationProcessor;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsStatisticMedianAggregationProcessor extends
        AbstractParallelGroupedDataStoringAggregationProcessor<ManeuverSpeedDetailsStatistic, ManeuverSpeedDetailsAggregation> {

    private static final String MESSAGE_KEY = "MedianTrendForTWAs";
    private final ManeuverSpeedDetailsStatisticAggregationProcessorHelper helper = new ManeuverSpeedDetailsStatisticAggregationProcessorHelper(
            MedianManeuverSpeedDetailsAggregationCreator.class);

    public ManeuverSpeedDetailsStatisticMedianAggregationProcessor(ExecutorService executor,
            Collection<Processor<Map<GroupKey, ManeuverSpeedDetailsAggregation>, ?>> resultReceivers) {
        super(executor, resultReceivers, MESSAGE_KEY);
    }

    private static final AggregationProcessorDefinition<ManeuverSpeedDetailsStatistic, ManeuverSpeedDetailsAggregation> DEFINITION = new SimpleAggregationProcessorDefinition<>(
            ManeuverSpeedDetailsStatistic.class, ManeuverSpeedDetailsAggregation.class, MESSAGE_KEY,
            ManeuverSpeedDetailsStatisticMedianAggregationProcessor.class);

    public static AggregationProcessorDefinition<ManeuverSpeedDetailsStatistic, ManeuverSpeedDetailsAggregation> getDefinition() {
        return DEFINITION;
    }

    @Override
    protected void storeElement(GroupedDataEntry<ManeuverSpeedDetailsStatistic> element) {
        helper.storeElement(element);
    }

    @Override
    protected Map<GroupKey, ManeuverSpeedDetailsAggregation> aggregateResult() {
        return helper.aggregateResult();
    }

    static class MedianManeuverSpeedDetailsAggregationCreator implements ManeuverSpeedDetailsAggregationCreator {
        private int count = 0;
        private ArrayList<Double>[] valuesPerTWA = initValuesPerAngle();

        private static ArrayList<Double>[] initValuesPerAngle() {
            @SuppressWarnings("unchecked")
            ArrayList<Double>[] valuesPerAngle = new ArrayList[360];
            for (int i = 0; i < 360; ++i) {
                valuesPerAngle[i] = new ArrayList<>(1000);
            }
            return valuesPerAngle;
        }

        public void addElement(ManeuverSpeedDetailsStatistic dataEntry) {
            double[] maneuverValuePerTWA = dataEntry.getManeuverValuePerTWA();
            for (int i = 0; i < maneuverValuePerTWA.length; ++i) {
                if (maneuverValuePerTWA[i] != 0) {
                    valuesPerTWA[i].add(maneuverValuePerTWA[i]);
                }
            }
            count++;
        }

        public ManeuverSpeedDetailsAggregationImpl aggregateResult() {
            double[] valuePerTWA = new double[360];
            int[] countPerTWA = new int[360];
            for (int i = 0; i < 360; ++i) {
                ArrayList<Double> valuesList = valuesPerTWA[i];
                int size = valuesList.size();
                if (size > 0) {
                    double[] values = new double[size];
                    int j = 0;
                    for (double value : valuesList) {
                        values[j++] = value;
                    }
                    Arrays.sort(values);

                    int middle = size / 2;
                    if (size % 2 == 1) {
                        valuePerTWA[i] = values[middle];
                    } else {
                        valuePerTWA[i] = (values[middle - 1] + values[middle]) / 2.0;
                    }
                }
                countPerTWA[i] = size;
            }

            return new ManeuverSpeedDetailsAggregationImpl(valuePerTWA, countPerTWA, count);
        }
    }
}
