package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PositionDAO implements IsSerializable {
    public double latDeg;
    public double lngDeg;
    
    public PositionDAO() {}
    
    public PositionDAO(double latDeg, double lngDeg) {
        this.latDeg = latDeg;
        this.lngDeg = lngDeg;
    }

    @Override
    public String toString() {
        return "("+latDeg+", "+lngDeg+")";
    }
}
