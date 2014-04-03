package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.WindStepping;

public class WindSpeedLevel {

    private final int level;
    private final WindStepping stepping;

    public WindSpeedLevel(int level, WindStepping stepping) {
        this.level = level;
        this.stepping = stepping;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + ((stepping == null) ? 0 : stepping.hashCode());
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
        WindSpeedLevel other = (WindSpeedLevel) obj;
        if (level != other.level)
            return false;
        if (stepping == null) {
            if (other.stepping != null)
                return false;
        } else if (!stepping.equals(other.stepping))
            return false;
        return true;
    }


}
