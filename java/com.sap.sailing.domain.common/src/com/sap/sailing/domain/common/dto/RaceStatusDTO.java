package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.TrackedRaceStatusEnum;

public class RaceStatusDTO implements Serializable {
    private static final long serialVersionUID = -5665104615048232751L;
    public TrackedRaceStatusEnum status;
    public double loadingProgress;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(loadingProgress);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        RaceStatusDTO other = (RaceStatusDTO) obj;
        if (Double.doubleToLongBits(loadingProgress) != Double.doubleToLongBits(other.loadingProgress))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return status.name() + (status==TrackedRaceStatusEnum.LOADING ? " ("+((int) (loadingProgress*100))+"%)" : "");
    }
}
