package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * This replicates a whole {@link DynamicSensorFixTrack}. Other tracks like {@link GPSFixTrack} are created on the
 * replica on the fly when the first fix is replicated. For sensor tracks there could be several different
 * {@link DynamicSensorFixTrack} implementations for different named tracks. This makes it necessary to serialize the
 * whole track to the replica to ensure that the track provides the correct functionality.
 */
public class RecordCompetitorSensorFixTrack extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -7092704633177037511L;
    private DynamicSensorFixTrack<Competitor, ?> track;
    
    public RecordCompetitorSensorFixTrack(RegattaAndRaceIdentifier raceIdentifier, DynamicSensorFixTrack<Competitor, ?> track) {
        super(raceIdentifier);
        this.track = track;
    }

    /**
     * Operations of this type can be run in parallel to other operations; subsequent operations do not have to wait
     * for this operation's completion.
     */
    @Override
    public boolean requiresSynchronousExecution() {
        return false;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getExistingTrackedRace(getRaceIdentifier());
	if (trackedRace != null) {
            trackedRace.addSensorTrack(track.getTrackedItem(), track.getTrackName(), track);
	}
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
