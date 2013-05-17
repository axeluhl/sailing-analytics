package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.google.gwt.i18n.client.NumberFormat;

public class PositionDTO implements Serializable {
    private static final long serialVersionUID = -8799012230990258044L;
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
