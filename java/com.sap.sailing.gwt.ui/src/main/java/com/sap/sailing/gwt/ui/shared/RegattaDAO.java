package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaDAO implements IsSerializable {
    public BoatClassDAO boatClass;
    public List<RaceDAO> races;
    
    public RegattaDAO() {}

    public RegattaDAO(BoatClassDAO boatClass, List<RaceDAO> races) {
        super();
        this.boatClass = boatClass;
        this.races = races;
    }
    
}
