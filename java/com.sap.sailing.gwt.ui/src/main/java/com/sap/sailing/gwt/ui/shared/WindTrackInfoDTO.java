package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindTrackInfoDTO implements IsSerializable {
    public List<WindDTO> windFixes;
    public long dampeningIntervalInMilliseconds;
    public double minWindConfidence;
    public double maxWindConfidence;
    
    public WindTrackInfoDTO() {}
}
