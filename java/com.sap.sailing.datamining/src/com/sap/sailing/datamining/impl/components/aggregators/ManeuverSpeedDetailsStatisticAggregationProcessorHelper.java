package com.sap.sailing.datamining.impl.components.aggregators;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.data.ManeuverSpeedDetailsStatistic;
import com.sap.sailing.datamining.impl.components.ManeuverSpeedDetailsUtils;
import com.sap.sailing.datamining.impl.data.ManeuverSpeedDetailsStatisticImpl;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregationImpl;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsStatisticAggregationProcessorHelper {

    private final Map<GroupKey, ManeuverSpeedDetailsAggregationCreatorHelper> resultMap = new HashMap<>();
    private Class<?> aggregationCreatorClass;

    public ManeuverSpeedDetailsStatisticAggregationProcessorHelper(Class<?> aggregationCreatorClass) {
        this.aggregationCreatorClass = aggregationCreatorClass;
    }

    protected void storeElement(GroupedDataEntry<ManeuverSpeedDetailsStatistic> element) {
        ManeuverSpeedDetailsStatistic statistic = element.getDataEntry();
        ManeuverSpeedDetailsSettings settings = statistic.getSettings();
        if (settings.isNormalizeManeuverDirection()
                && statistic.getManeuverDirection() != settings.getNormalizedManeuverDirection()) {
            double[] newValuesPerTWA = ManeuverSpeedDetailsUtils
                    .flipManeuversDirection(statistic.getManeuverValuePerTWA());
            statistic = new ManeuverSpeedDetailsStatisticImpl(newValuesPerTWA, statistic.getManeuverDirection(),
                    statistic.getSettings());
        }

        ManeuverSpeedDetailsAggregationCreatorHelper speedDetailsAggregationHelper = resultMap.get(element.getKey());
        if (speedDetailsAggregationHelper == null) {
            speedDetailsAggregationHelper = new ManeuverSpeedDetailsAggregationCreatorHelper();
            resultMap.put(element.getKey(), speedDetailsAggregationHelper);
        }
        speedDetailsAggregationHelper.addElement(statistic);
    }

    protected Map<GroupKey, ManeuverSpeedDetailsAggregation> aggregateResult() {
        Map<GroupKey, ManeuverSpeedDetailsAggregation> convertedResultMap = new HashMap<>();
        for (Entry<GroupKey, ManeuverSpeedDetailsAggregationCreatorHelper> entry : resultMap.entrySet()) {
            ManeuverSpeedDetailsAggregation aggregation = entry.getValue().aggregateResult();
            if (aggregation != null) {
                convertedResultMap.put(entry.getKey(), aggregation);
            }
        }
        return convertedResultMap;
    }

    private class ManeuverSpeedDetailsAggregationCreatorHelper implements ManeuverSpeedDetailsAggregationCreator {

        private ManeuverSpeedDetailsAggregationCreator allManeuvers = null;
        private ManeuverSpeedDetailsAggregationCreator portsideManeuvers = null;
        private ManeuverSpeedDetailsAggregationCreator starboardManeuvers = null;

        public void addElement(ManeuverSpeedDetailsStatistic statistic) {
            if (statistic.getSettings().isManeuverDirectionEqualWeightingEnabled()) {
                switch (statistic.getManeuverDirection()) {
                case PORT:
                    if (portsideManeuvers == null) {
                        try {
                            portsideManeuvers = (ManeuverSpeedDetailsAggregationCreator) aggregationCreatorClass
                                    .newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                        }
                    }
                    portsideManeuvers.addElement(statistic);
                case STARBOARD:
                    if (starboardManeuvers == null) {
                        try {
                            starboardManeuvers = (ManeuverSpeedDetailsAggregationCreator) aggregationCreatorClass
                                    .newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                        }
                    }
                    starboardManeuvers.addElement(statistic);
                }
            } else {
                if (allManeuvers == null) {
                    try {
                        allManeuvers = (ManeuverSpeedDetailsAggregationCreator) aggregationCreatorClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                    }
                }
                allManeuvers.addElement(statistic);
            }
        }

        public ManeuverSpeedDetailsAggregation aggregateResult() {
            if (allManeuvers != null) {
                return allManeuvers.aggregateResult();
            }
            if (starboardManeuvers == null) {
                return portsideManeuvers != null ? portsideManeuvers.aggregateResult() : null;
            }
            if (portsideManeuvers == null) {
                return starboardManeuvers != null ? starboardManeuvers.aggregateResult() : null;
            }

            ManeuverSpeedDetailsAggregation starboardResult = starboardManeuvers.aggregateResult();
            ManeuverSpeedDetailsAggregation portsideResult = portsideManeuvers.aggregateResult();
            int count = starboardResult.getCount() + portsideResult.getCount();
            int[] countPerTWA = new int[360];
            double[] valuePerTWA = new double[360];
            int[] starboardCountPerTWA = starboardResult.getCountPerTWA();
            int[] portsideCountPerTWA = portsideResult.getCountPerTWA();
            double[] starboardValuePerTWA = starboardResult.getValuePerTWA();
            double[] portsideValuePerTWA = portsideResult.getValuePerTWA();
            for (int i = 0; i < 360; i++) {
                countPerTWA[i] = starboardCountPerTWA[i] + portsideCountPerTWA[i];
                double divisor = starboardValuePerTWA[i] == 0 || portsideValuePerTWA[i] == 0 ? 1 : 2;
                valuePerTWA[i] = (starboardValuePerTWA[i] + portsideValuePerTWA[i]) / divisor;
            }
            return new ManeuverSpeedDetailsAggregationImpl(valuePerTWA, countPerTWA, count);
        }

    }

    interface ManeuverSpeedDetailsAggregationCreator {
        void addElement(ManeuverSpeedDetailsStatistic dataEntry);

        ManeuverSpeedDetailsAggregation aggregateResult();
    }

}
