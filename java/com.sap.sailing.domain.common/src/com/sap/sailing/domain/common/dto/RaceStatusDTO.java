package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sailing.domain.common.TrackedRaceStatusEnum;

public class RaceStatusDTO implements Serializable {
    private static final long serialVersionUID = -5665104615048232751L;
    public TrackedRaceStatusEnum status;
    public double loadingProgress;
    
    @Override
    public String toString() {
        return status.name() + (status==TrackedRaceStatusEnum.LOADING ? " ("+((int) (loadingProgress*100))+"%)" : "");
    }
}
