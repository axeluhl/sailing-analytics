package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.interfaces.RacingEventService;

public class RemoveMediaTrackOperation extends AbstractMediaTrackOperation {

    private static final long serialVersionUID = 1L;
    
    public RemoveMediaTrackOperation(MediaTrack mediaTrack) {
        super(mediaTrack);
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.mediaTrackDeleted(this.mediaTrack);
        return null;
    }

}

