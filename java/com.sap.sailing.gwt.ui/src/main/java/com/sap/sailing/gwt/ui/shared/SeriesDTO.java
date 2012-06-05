package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SeriesDTO extends NamedDTO implements IsSerializable {
    private List<FleetDTO> fleets;
    private List<String> raceColumnNames;
    private boolean isFleetsOrdered;
    private boolean isMedal;
    
    public SeriesDTO() {}
    
    public SeriesDTO(String name, List<FleetDTO> fleets, List<String> raceColumnNames, boolean isFleetsOrdered, boolean isMedal) {
        super(name);
        this.fleets = fleets;
        this.raceColumnNames = raceColumnNames;
        this.isFleetsOrdered = isFleetsOrdered;
    }
    
    public Iterable<String> getRaceColumnNames() {
        return raceColumnNames;
    }

    public boolean isFleetsOrdered() {
        return isFleetsOrdered;
    }
    
    public boolean isMedal() {
        return isMedal;
    }

    public Iterable<FleetDTO> getFleets() {
        return fleets;
    }
}
