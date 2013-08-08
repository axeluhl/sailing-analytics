package com.sap.sailing.datamining.impl.gpsfix;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.impl.AbstractDimension;

public abstract class GPSFixDimension extends AbstractDimension<GPSFixWithContext> {

    private String name;

    public GPSFixDimension(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GPSFixDimension other = (GPSFixDimension) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
