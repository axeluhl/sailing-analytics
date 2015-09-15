package com.sap.sailing.polars.datamining.shared;


public class PolarAggregationImpl implements PolarAggregation {
    
    private static final long serialVersionUID = 9177124509619315748L;
    private double[] sumSpeedsPerAngle = new double[360];
    private double[] countPerAngle = new double[360];

    @Override
    public void addElement(PolarStatistic dataEntry) {
        long roundedAngleDeg = Math.round(dataEntry.getTrueWindAngleDeg());
        int angleDeg = (int) roundedAngleDeg;
        if (angleDeg < 0) {
            angleDeg = (360 + angleDeg);
        }
        sumSpeedsPerAngle[angleDeg] += dataEntry.getBoatSpeed().getKnots();
        countPerAngle[angleDeg]++;
    }
    
    @Override
    public double[] getAverageSpeedsPerAngle() {
        double[] averages = new double[360];
        for (int i = 0; i < 360; i++) {
            if (countPerAngle[i] > 0) {
                averages[i] = sumSpeedsPerAngle[i] / countPerAngle[i];
            }
        }
        return averages;
    }

}
