package com.sap.sailing.polars.datamining.shared;

import java.io.Serializable;

public interface PolarAggregation extends Serializable {

    void addElement(PolarStatistic dataEntry);

    double[] getAverageSpeedsPerAngle();

}
