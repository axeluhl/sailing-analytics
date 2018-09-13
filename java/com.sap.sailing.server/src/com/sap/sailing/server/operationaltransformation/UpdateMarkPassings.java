package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.Util;

public class UpdateMarkPassings extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = 8462323149468755367L;
    private static final Logger logger = Logger.getLogger(UpdateMarkPassings.class.getName());
    private final Competitor competitor;
    private final Iterable<MarkPassing> markPassings;
    
    public UpdateMarkPassings(RegattaAndRaceIdentifier raceIdentifier, Competitor competitor,
            Iterable<MarkPassing> markPassings) {
        super(raceIdentifier);
        this.competitor = competitor;
        // ensure that markPassings is a serializable collection; Iterable could, e.g., also
        // be a TreeMap.Values collection or similar. See also bug 4746.
        final ArrayList<MarkPassing> markPassingsList = new ArrayList<>();
        Util.addAll(markPassings, markPassingsList);
        this.markPassings = markPassingsList;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getExistingTrackedRace(getRaceIdentifier());
        if (trackedRace != null) {
            trackedRace.updateMarkPassings(competitor, markPassings);
        } else {
            logger.warning("Tracked race for "+getRaceIdentifier()+" has disappeared");
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
