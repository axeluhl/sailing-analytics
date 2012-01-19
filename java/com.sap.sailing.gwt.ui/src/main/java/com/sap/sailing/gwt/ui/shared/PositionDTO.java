package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PositionDTO implements IsSerializable {
    public double latDeg;
    public double lngDeg;
    
    public PositionDTO() {}
    
    public PositionDTO(double latDeg, double lngDeg) {
        this.latDeg = latDeg;
        this.lngDeg = lngDeg;
    }

    @Override
    public String toString() {
        return "("+latDeg+", "+lngDeg+")";
    }
}
