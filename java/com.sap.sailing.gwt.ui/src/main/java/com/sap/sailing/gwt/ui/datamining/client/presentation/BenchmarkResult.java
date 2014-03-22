package com.sap.sailing.gwt.ui.datamining.client.presentation;

public class BenchmarkResult {
    
    private String identifier;
    private int numberOfGPSFixes;
    private double serverTime;
    private double overallTime;
    
    public BenchmarkResult(String identifier, int numberOfGPSFixes, double serverTime, double overallTime) {
        this.identifier = identifier;
        this.numberOfGPSFixes = numberOfGPSFixes;
        this.serverTime = serverTime;
        this.overallTime = overallTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }
    
    public double getServerTime() {
        return serverTime;
    }
    
    public double getOverallTime() {
        return overallTime;
    }

}
