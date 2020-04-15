package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.interfaces.RacingEventService;

public class UpdateMediaTrackRacesOperation extends AbstractMediaTrackOperation {

    private static final long serialVersionUID = 1L;
    
    public UpdateMediaTrackRacesOperation(MediaTrack mediaTrack) {
        super(mediaTrack);
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.mediaTrackAssignedRacesChanged(this.mediaTrack);
        return null;
    }

}

