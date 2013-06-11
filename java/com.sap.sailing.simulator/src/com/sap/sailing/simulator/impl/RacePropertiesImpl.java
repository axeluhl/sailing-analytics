package com.sap.sailing.simulator.impl;

import com.sap.sailing.simulator.RaceProperties;

public class RacePropertiesImpl implements RaceProperties {

    String name;
    String boatClass;
    String url;
    Integer index;
    
    public RacePropertiesImpl(String name, String boatClass, String url, Integer index) {
        this.name = name;
        this.boatClass = boatClass;
        this.url = url;
        this.index = index;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getBoatClass() {
        return this.boatClass;
    }
    
    public String getURL() {
        return this.url;
    }
    
    public Integer getIndex() {
        return this.index;
    }
    
}
