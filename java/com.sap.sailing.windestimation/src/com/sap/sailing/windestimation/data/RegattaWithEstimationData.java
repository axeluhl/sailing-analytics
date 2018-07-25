package com.sap.sailing.windestimation.data;

import java.util.List;

public class RegattaWithEstimationData<T> {

    private final String regattaName;
    private final List<RaceWithEstimationData<T>> races;

    public RegattaWithEstimationData(String regattaName, List<RaceWithEstimationData<T>> races) {
        this.regattaName = regattaName;
        this.races = races;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public List<RaceWithEstimationData<T>> getRaces() {
        return races;
    }

}
