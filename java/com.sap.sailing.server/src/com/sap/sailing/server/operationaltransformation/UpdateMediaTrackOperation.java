package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.RacingEventService;

public class UpdateMediaTrackOperation extends AbstractMediaTrackOperation {

    private static final long serialVersionUID = 1L;
    
    public UpdateMediaTrackOperation(MediaTrack mediaTrack) {
        super(mediaTrack);
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.mediaTrackChanged(this.mediaTrack);
        return null;
    }

}

