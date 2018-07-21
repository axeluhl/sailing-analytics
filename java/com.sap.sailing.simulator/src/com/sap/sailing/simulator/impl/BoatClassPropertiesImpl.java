package com.sap.sailing.simulator.impl;

import com.sap.sailing.simulator.BoatClassProperties;
import com.sap.sse.common.Distance;

public class BoatClassPropertiesImpl implements BoatClassProperties {

    String name;
    Distance length;
    String polar;
    Integer index;
    
    public BoatClassPropertiesImpl(String name, Distance length, String polar, Integer index) {
        this.name = name;
        this.length = length;
        this.polar = polar;
        this.index = index;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Distance getLength() {
        return this.length;
    }
    
    public String getPolar() {
        return this.polar;
    }
    
    public Integer getIndex() {
        return this.index;
    }
    
}
