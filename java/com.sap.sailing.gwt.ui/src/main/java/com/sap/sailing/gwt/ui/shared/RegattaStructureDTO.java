package com.sap.sailing.gwt.ui.shared;

import java.util.Set;

import buildstructure.BuildStructure;

import com.sap.sailing.xrr.structureimport.RegattaStructureKey;
import com.sap.sailing.xrr.structureimport.SeriesParameters;

public class RegattaStructureDTO {
    private final RegattaStructureKey regattaStructure;
    private SeriesDTO defaultSeries;
    private final Set<BuildStructure> linkedBuildStructures;

    public RegattaStructureDTO(RegattaStructureKey regattaStructure, Set<BuildStructure> linkedBuildStructures) {
        this.regattaStructure = regattaStructure;
        this.linkedBuildStructures = linkedBuildStructures;
        defaultSeries = new SeriesDTO("Default", null, null, false, null, false, false, false);
    }

    public String getRegattaStructureAsString() {
        String regattaStructureString = "";

        for (String s : regattaStructure.getRegattaStructureKey()) {
            regattaStructureString = regattaStructureString.concat("," + s);
        }
        return regattaStructureString.substring(1);
    }

    public void setDefaultSeries(RegattaDTO defaultRegatta) {
        SeriesParameters defaultSeries = new SeriesParameters(false, false, false, null);
        if (defaultRegatta.series.size() > 0) { // null abfangen
            SeriesDTO series = defaultRegatta.series.get(0);
            defaultSeries
                    .setFirstColumnIsNonDiscardableCarryForward(series.isFirstColumnIsNonDiscardableCarryForward());
            defaultSeries.setHasSplitFleetContiguousScoring(series.hasSplitFleetContiguousScoring());
            defaultSeries.setStartswithZeroScore(series.isStartsWithZeroScore());
            defaultSeries.setDiscardingThresholds(series.getDiscardThresholds());
        }
    }

    public SeriesDTO getDefaultSeries() {
        return defaultSeries;
    }

    public Set<BuildStructure> getLinkedBuildStructures() {
        return linkedBuildStructures;
    }
    
    

}
