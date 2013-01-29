package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;

public class RaceStatusDTO implements IsSerializable {
    public TrackedRaceStatusEnum status;
    public double loadingProgress;
    
    @Override
    public String toString() {
        return status.name() + (status==TrackedRaceStatusEnum.LOADING ? " ("+((int) (loadingProgress*100))+"%)" : "");
    }
}
