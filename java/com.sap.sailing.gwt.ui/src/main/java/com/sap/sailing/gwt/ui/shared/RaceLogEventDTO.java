package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceLogEventDTO implements IsSerializable {
    
    public long timePoint; 
    public int passId;
    
    // for GWT serialization
    protected RaceLogEventDTO() {}
    
    public RaceLogEventDTO(long timePoint, int passId) {
        this.timePoint = timePoint;
        this.passId = passId;
    }

}
