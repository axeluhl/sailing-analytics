package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DeprecatedRegattaDTO implements IsSerializable {
    public BoatClassDTO boatClass;
    public List<RaceDTO> races;

    private RegattaDTO regatta;
    
    public DeprecatedRegattaDTO() {}

    public DeprecatedRegattaDTO(BoatClassDTO boatClass, List<RaceDTO> races) {
        super();
        this.boatClass = boatClass;
        this.races = races;
    }

    public RegattaDTO getRegatta() {
        return regatta;
    }

    public void setRegatta(RegattaDTO regatta) {
        this.regatta = regatta;
    }
    
}
