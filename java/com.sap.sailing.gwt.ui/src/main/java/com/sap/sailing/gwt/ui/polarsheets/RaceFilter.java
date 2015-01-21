package com.sap.sailing.gwt.ui.polarsheets;

import com.sap.sailing.domain.common.dto.RaceDTO;

public class RaceFilter {
    
    private Boolean withGpsFixes;
    private Boolean withWindData;
    
    
    

    public RaceFilter(Boolean withGpsFixes, Boolean withWindData) {
        this.withGpsFixes = withGpsFixes;
        this.withWindData = withWindData;
    }




    public boolean compliesToFilter(RaceDTO race) {
        if (!compliesToGpsFixFilter(race) || !compliesToWindDataFilter(race)) {
            return false;
        }
        return true;
    }
    
    
    private boolean compliesToGpsFixFilter(RaceDTO race) {
        if (withGpsFixes!=null) {
            if (race.trackedRace == null || race.trackedRace.hasGPSData != withGpsFixes) {
                return false;
            }
        }
        return true;
    }
    
    private boolean compliesToWindDataFilter(RaceDTO race) {
        if (withGpsFixes!=null) {
            if (race.trackedRace == null || race.trackedRace.hasWindData != withWindData) {
                return false;
            }
        }
        return true;
    }

}
