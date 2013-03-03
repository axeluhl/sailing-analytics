package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceEventDTO implements IsSerializable {
    public String eventName;
    
    // for GWT serialization
    protected RaceEventDTO() {}
    
    public RaceEventDTO(String event) {
       this.eventName = event;
    }

}
