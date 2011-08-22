package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindTrackInfoDAO implements IsSerializable {
    public List<WindDAO> windFixes;
    public long dampeningIntervalInMilliseconds;
    
    public WindTrackInfoDAO() {}
}
