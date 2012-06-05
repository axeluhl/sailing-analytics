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
    }
    
    /**
     * Names of this series' fleets, if ordered then from best to worst (best first, worst last) 
     */
    public Iterable<String> getRaceColumnNames() {
        return raceColumnNames;
    }

    public boolean isMedal() {
        return isMedal;
    }

    public Iterable<FleetDTO> getFleets() {
        return fleets;
    }
}
