package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;

public interface Aggregator extends Serializable {
    
    public double aggregate(List<Double> data);

}
