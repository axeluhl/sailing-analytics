package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceEventLogDTO implements IsSerializable {
    public String raceName;
    
    public List<RaceEventDTO> raceEvents;

    // for GWT serialization
    protected RaceEventLogDTO() {}
    
    public RaceEventLogDTO(String raceName) {
       this.raceName = raceName;
       raceEvents = new ArrayList<RaceEventDTO>();
    }

}
