package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceInfoDTO implements IsSerializable {
    public String raceName;
    public String fleet;
    public Date startTime;
    
    // for GWT serialization
    public RaceInfoDTO() { }
    
}
