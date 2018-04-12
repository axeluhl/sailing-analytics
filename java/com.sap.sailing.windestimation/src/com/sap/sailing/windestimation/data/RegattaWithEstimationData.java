package com.sap.sailing.windestimation.data;

import java.util.List;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RegattaWithEstimationData {

    private final String regattaName;
    private final List<RaceWithEstimationData> races;

    public RegattaWithEstimationData(String regattaName, List<RaceWithEstimationData> races) {
        this.regattaName = regattaName;
        this.races = races;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public List<RaceWithEstimationData> getRaces() {
        return races;
    }

}
