package com.sap.sailing.gwt.ui.datamining.presentation;

public class BenchmarkResult {
    
    private String identifier;
    private int dataAmount;
    private double serverTime;
    private double overallTime;
    
    public BenchmarkResult(String identifier, int dataAmount, double serverTime, double overallTime) {
        this.identifier = identifier;
        this.dataAmount = dataAmount;
        this.serverTime = serverTime;
        this.overallTime = overallTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getDataAmount() {
        return dataAmount;
    }
    
    public double getServerTime() {
        return serverTime;
    }
    
    public double getOverallTime() {
        return overallTime;
    }

}
