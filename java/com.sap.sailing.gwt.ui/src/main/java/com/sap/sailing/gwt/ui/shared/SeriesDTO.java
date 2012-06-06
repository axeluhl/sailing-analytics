package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SeriesDTO extends NamedDTO implements IsSerializable {
    private List<FleetDTO> fleets;
    private List<String> raceColumnNames;
    private boolean isMedal;
    
    public SeriesDTO() {}
    
    public SeriesDTO(String name, List<FleetDTO> fleets, List<String> raceColumnNames, boolean isMedal) {
        super(name);
        this.fleets = fleets;
        this.raceColumnNames = raceColumnNames;
        this.isMedal = isMedal;
    }
    
    /**
     * Names of this series' fleets, if ordered then from best to worst (best first, worst last) 
     */
    public List<String> getRaceColumnNames() {
        return raceColumnNames;
    }

    public boolean isMedal() {
        return isMedal;
    }

    public List<FleetDTO> getFleets() {
        return fleets;
    }

    public void setFleets(List<FleetDTO> fleets) {
        this.fleets = fleets;
    }

    public void setRaceColumnNames(List<String> raceColumnNames) {
        this.raceColumnNames = raceColumnNames;
    }

    public void setMedal(boolean isMedal) {
        this.isMedal = isMedal;
    }
}
