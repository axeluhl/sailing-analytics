package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GPSFixDAO implements IsSerializable {
    public Date timepoint;
    public PositionDAO position;
    
    public GPSFixDAO() {}

    public GPSFixDAO(Date timepoint, PositionDAO position) {
        super();
        this.timepoint = timepoint;
        this.position = position;
    }
}
