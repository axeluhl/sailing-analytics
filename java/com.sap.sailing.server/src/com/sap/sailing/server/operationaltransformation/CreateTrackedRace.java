package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;

/**
 * Creates a tracked race for a race identifier by a {@link RaceIdentifier}. The operation assumes that the
 * {@link RaceDefinition}, therefore the {@link Event} as well as the {@link TrackedEvent} into which the
 * new {@link TrackedRace} will be composed already exist and that the {@link TrackedRace} does not yet
 * exist.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CreateTrackedRace extends AbstractRaceOperation {
    private static final long serialVersionUID = 5084401060896514911L;
    private final long millisecondsOverWhichToAverageWind;
    private final long millisecondsOverWhichToAverageSpeed;

    public CreateTrackedRace(EventAndRaceIdentifier raceIdentifier, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super(raceIdentifier);
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.createTrackedRace(getRaceIdentifier(), EmptyWindStore.INSTANCE, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed);
        return toState;
    }

}
