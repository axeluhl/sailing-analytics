package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;

/**
 * Starts out in state {@link TrackedRaceStatusEnum#PREPARED} with loading progress 0.0.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TrackedRaceStatusImpl implements TrackedRaceStatus {
    private static final long serialVersionUID = 6443697957620826443L;
    private final TrackedRaceStatusEnum status;
    private final double loadingProgress;
    
    public TrackedRaceStatusImpl(TrackedRaceStatusEnum status, double loadingProgress) {
        super();
        this.status = status;
        this.loadingProgress = loadingProgress;
    }

    @Override
    public TrackedRaceStatusEnum getStatus() {
        return status;
    }

    @Override
    public double getLoadingProgress() {
        return loadingProgress;
    }
    
    @Override
    public String toString() {
        return ""+status+" ("+loadingProgress+")";
    }
}
