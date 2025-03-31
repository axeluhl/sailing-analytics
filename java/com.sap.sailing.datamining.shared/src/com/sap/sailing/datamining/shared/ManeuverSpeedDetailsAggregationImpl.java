package com.sap.sailing.datamining.shared;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsAggregationImpl implements ManeuverSpeedDetailsAggregation {
    private static final long serialVersionUID = 9177124509619315750L;
    private double[] valuePerTWA;
    private int[] countPerTWA;
    private int count;

    public ManeuverSpeedDetailsAggregationImpl() {
        // GWT
    }

    public ManeuverSpeedDetailsAggregationImpl(double[] valuePerTWA, int[] countPerTWA, int count) {
        this.valuePerTWA = valuePerTWA;
        this.countPerTWA = countPerTWA;
        this.count = count;
    }

    @Override
    public double[] getValuePerTWA() {
        return valuePerTWA;
    }

    @Override
    public int[] getCountPerTWA() {
        return countPerTWA;
    }

    @Override
    public int getCount() {
        return count;
    }

}
