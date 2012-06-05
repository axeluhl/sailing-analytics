package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SeriesDTO extends NamedDTO implements IsSerializable {
    private List<String> fleetNames;
    private List<String> raceColumnNames;
    private boolean isMedal;
    
    SeriesDTO() {} // for serializability
    
    public SeriesDTO(String name, List<String> fleetNames, List<String> raceColumnNames, boolean isMedal) {
        super(name);
        this.fleetNames = fleetNames;
        this.raceColumnNames = raceColumnNames;
    }
    
    /**
     * Names of this series' fleets, if ordered then from best to worst (best first, worst last) 
     */
    public Iterable<String> getFleetNames() {
        return fleetNames;
    }
    
    public Iterable<String> getRaceColumnNames() {
        return raceColumnNames;
    }

    public boolean isMedal() {
        return isMedal;
    }

}
