package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.i18n.client.NumberFormat;
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

    public String toFormattedString() {
        NumberFormat fmt = NumberFormat.getFormat("#.###");
        return fmt.format(latDeg)+", "+fmt.format(lngDeg);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = (int) latDeg;
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = (int) lngDeg;
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
