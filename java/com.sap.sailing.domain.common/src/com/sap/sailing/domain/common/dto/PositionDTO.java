package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class PositionDTO implements Serializable {
    private static final long serialVersionUID = -8799012230990258044L;
    private double latDeg;
    private double lngDeg;
    
    PositionDTO() {}
    
    public PositionDTO(double latDeg, double lngDeg) {
        this.setLatDeg(latDeg);
        this.setLngDeg(lngDeg);
    }

    @Override
    public String toString() {
        return "("+getLatDeg()+", "+getLngDeg()+")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = (int) getLatDeg();
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = (int) getLngDeg();
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public double getLatDeg() {
        return latDeg;
    }

    public void setLatDeg(double latDeg) {
        this.latDeg = latDeg;
    }

    public double getLngDeg() {
        return lngDeg;
    }

    public void setLngDeg(double lngDeg) {
        this.lngDeg = lngDeg;
    }
}
