package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.interfaces.RacingEventService;

public class UpdateMediaTrackUrlOperation extends AbstractMediaTrackOperation {

    private static final long serialVersionUID = 1L;
    
    public UpdateMediaTrackUrlOperation(MediaTrack mediaTrack) {
        super(mediaTrack);
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.mediaTrackUrlChanged(this.mediaTrack);
        return null;
    }

}

