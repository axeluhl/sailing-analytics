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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            if (o instanceof PositionDTO) {
                PositionDTO other = (PositionDTO) o;
                return Math.abs(this.latDeg-other.latDeg) <= 1e-5 && Math.abs(this.lngDeg-other.lngDeg) <= 1e-5;
            }
            return false;
        }
    }
  
}
