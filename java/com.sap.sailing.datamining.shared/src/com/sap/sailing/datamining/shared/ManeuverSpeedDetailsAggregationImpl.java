package com.sap.sailing.datamining.shared;

public class ManeuverSpeedDetailsAggregationImpl implements ManeuverSpeedDetailsAggregation {
    private static final long serialVersionUID = 9177124509619315750L;
    private double[] valuePerAngle;
    private int[] countPerAngle;
    private int count;
    
    public ManeuverSpeedDetailsAggregationImpl() {
      //GWT
    }
    
    public ManeuverSpeedDetailsAggregationImpl(double[] valuePerAngle, int[] countPerAngle, int count) {
        this.valuePerAngle = valuePerAngle;
        this.countPerAngle = countPerAngle;
        this.count = count;
    }

    @Override
    public double[] getValuePerAngle() {
        return valuePerAngle;
    }

    @Override
    public int[] getCountPerAngle() {
        return countPerAngle;
    }

    @Override
    public int getCount() {
        return count;
    }

}

