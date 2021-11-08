package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.interfaces.RacingEventService;

public class UpdateMediaTrackTitleOperation extends AbstractMediaTrackOperation {

    private static final long serialVersionUID = 1L;
    
    public UpdateMediaTrackTitleOperation(MediaTrack mediaTrack) {
        super(mediaTrack);
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.mediaTrackTitleChanged(this.mediaTrack);
        return null;
    }

}

