package com.sap.sailing.server.operationaltransformation;

import java.util.Collection;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddMediaTrackBatchOperation extends AbstractRacingEventServiceOperation<Void> {

    private static final long serialVersionUID = 1L;
    
    private final Collection<MediaTrack> mediaTracks;
    
    public AddMediaTrackBatchOperation(Collection<MediaTrack> mediaTracks) {
        this.mediaTracks = mediaTracks;
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.mediaTracksAdded(this.mediaTracks);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null; //no op
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null; //no op
    }
    
}
