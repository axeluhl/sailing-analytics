package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SeriesDTO extends NamedDTO implements IsSerializable {
    private List<String> fleetNames;
    private List<String> raceColumnNames;
    private boolean isFleetsOrdered;
    private boolean isMedal;
    
    SeriesDTO() {} // for serializability
    
    public SeriesDTO(String name, List<String> fleetNames, List<String> raceColumnNames, boolean isFleetsOrdered, boolean isMedal) {
        super(name);
        this.fleetNames = fleetNames;
        this.raceColumnNames = raceColumnNames;
        this.isFleetsOrdered = isFleetsOrdered;
    }
    
    public Iterable<String> getFleetNames() {
        return fleetNames;
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

}
