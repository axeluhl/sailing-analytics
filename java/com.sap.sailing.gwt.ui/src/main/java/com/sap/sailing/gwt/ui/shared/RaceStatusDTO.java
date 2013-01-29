package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.lifecycle.impl.TrackedRaceState;

public class RaceStatusDTO implements IsSerializable {
    public TrackedRaceState status;
    public double loadingProgress;
    
    @Override
    public String toString() {
        return status.name() + (status==TrackedRaceState.LOADING_STORED_DATA ? " ("+((int) (loadingProgress*100))+"%)" : "");
    }
}
