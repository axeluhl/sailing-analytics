package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.RacingEventServiceOperation;

public abstract class AbstractMediaTrackOperation extends AbstractRacingEventServiceOperation<Void> {

    private static final long serialVersionUID = 1L;
    
    protected final MediaTrack mediaTrack;

    public AbstractMediaTrackOperation(MediaTrack mediaTrack) {
        this.mediaTrack = mediaTrack;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }

}