package com.sap.sailing.polars.datamining.shared;



public class PolarAggregationImpl implements PolarAggregation {
    
    private static final long serialVersionUID = 9177124509619315748L;
    private double[] sumSpeedsPerAngle = new double[360];
    private int[] countPerAngle = new int[360];
    private int[][] histogramData;
    private int count = 0;
    private PolarDataMiningSettings settings;
    
    public PolarAggregationImpl() {
        //GWT
    }
    
    public PolarAggregationImpl(PolarDataMiningSettings polarDataMiningSettings) {
        this.settings = polarDataMiningSettings;
        histogramData = new int[360][];
        for (int i = 0; i < 360; i++) {
            histogramData[i] = new int[settings.getNumberOfHistogramColumns()];
        }
    }

    @Override
    public void addElement(PolarStatistic dataEntry) {
        long roundedAngleDeg = Math.round(dataEntry.getTrueWindAngleDeg());
        int angleDeg = (int) roundedAngleDeg;
        if (angleDeg < 0) {
            angleDeg = (360 + angleDeg);
        }
        sumSpeedsPerAngle[angleDeg] += dataEntry.getBoatSpeed().getKnots();
        countPerAngle[angleDeg]++;
        // TODO add 1 to correct histogram column, for that we need to know min and max
        //histogramData[angleDeg][]
        count++;
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

    @Override
    public int[] getCountPerAngle() {
        return countPerAngle;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public PolarDataMiningSettings getSettings() {
        return settings;
    }
    
    

}
