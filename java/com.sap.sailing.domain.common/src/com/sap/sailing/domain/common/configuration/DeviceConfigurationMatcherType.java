package com.sap.sailing.domain.common.configuration;

public enum DeviceConfigurationMatcherType {
    
    SINGLE(1);

    private int matchingRank;

    private DeviceConfigurationMatcherType(int rank) {
        this.matchingRank = rank;
    }

    public int getRank() {
        return matchingRank;
    }
}