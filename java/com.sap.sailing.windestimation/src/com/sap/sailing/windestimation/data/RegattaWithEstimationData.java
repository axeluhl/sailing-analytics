package com.sap.sailing.windestimation.data;

import java.util.List;

/**
 * Regatta with races which has been fetched during data import.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 *            The type of elements within a competitor track. E.g. maneuver, or gps-fix.
 */
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
