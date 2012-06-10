package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SeriesDTO extends NamedDTO implements IsSerializable {
    private List<FleetDTO> fleets;
    private List<RaceColumnDTO> raceColumns;
    private boolean isMedal;
    
    public SeriesDTO() {}
    
    public SeriesDTO(String name, List<FleetDTO> fleets, List<RaceColumnDTO> raceColumns, boolean isMedal) {
        super(name);
        this.fleets = fleets;
        this.raceColumns = raceColumns;
        this.isMedal = isMedal;
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

    public void setMedal(boolean isMedal) {
        this.isMedal = isMedal;
    }

    public List<RaceColumnDTO> getRaceColumns() {
        return raceColumns;
    }

    public void setRaceColumns(List<RaceColumnDTO> raceColumns) {
        this.raceColumns = raceColumns;
    }
}
