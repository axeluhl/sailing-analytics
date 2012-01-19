package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaDTO implements IsSerializable {
    public BoatClassDTO boatClass;
    public List<RaceDTO> races;
    
    public RegattaDTO() {}

    public RegattaDTO(BoatClassDTO boatClass, List<RaceDTO> races) {
        super();
        this.boatClass = boatClass;
        this.races = races;
    }
    
}
