package com.sap.sailing.server.operationaltransformation;

import java.util.HashSet;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public abstract class AbstractMediaTrackOperation extends AbstractRacingEventServiceOperation<Void> {

    private static final long serialVersionUID = 1L;
    
    protected final MediaTrack mediaTrack;

    /**
     * Creates a deep (including cloning the {@link MediaTrack#assignedRaces} set) clone of the {@code mediaTrack} object
     * so that when serializing multiple such operations for the same {@link MediaTrack} into the same {@link ObjectOutputStream}
     * we'll get separate copies still, reflecting the different states over time. See also bug 5741.
     */
    public AbstractMediaTrackOperation(MediaTrack mediaTrack) {
        this.mediaTrack = new MediaTrack(mediaTrack.dbId, mediaTrack.title, mediaTrack.url, mediaTrack.startTime,
                mediaTrack.duration, mediaTrack.mimeType, new HashSet<>(mediaTrack.assignedRaces));
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
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