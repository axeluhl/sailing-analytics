package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceInfoDTO implements IsSerializable {
    public String raceName;
    public String fleet;
    public Date startTime;
    public RaceLogRaceStatus lastStatus;
    public Flags lastFlag;
    public boolean displayed;
    
    // for GWT serialization
    public RaceInfoDTO() { }
    
}
