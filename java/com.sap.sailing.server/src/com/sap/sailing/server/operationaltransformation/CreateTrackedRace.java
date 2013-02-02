package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * Creates a tracked race for a race identifier by a {@link RaceIdentifier}. The operation assumes that the
 * {@link RaceDefinition}, therefore the {@link Regatta} as well as the {@link TrackedRegatta} into which the
 * new {@link TrackedRace} will be composed already exist and that the {@link TrackedRace} does not yet
 * exist.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CreateTrackedRace extends AbstractRaceOperation<TrackedRace> {
    private static final long serialVersionUID = 5084401060896514911L;
    private final long millisecondsOverWhichToAverageWind;
    private final long millisecondsOverWhichToAverageSpeed;
    private final long delayToLiveInMillis;
    
    /**
     * If a {@link WindStore} is provided to this command, it will be used for the construction of the tracked race.
     * However, after de-serialization, the wind store will always be <code>null</code>, causing the use of an
     * {@link EmptyWindStore}.
     */
    private transient final WindStore windStore;
    
    /**
     * If a {@link RaceLogStore} is provided to this command, it will be used for the construction of the tracked race.
     * However, after de-serialization, the race log store will always be <code>null</code>, causing the use of an
     * {@link EmptyRaceLogStore}.
     */
    private transient final RaceLogStore raceLogStore;

    /**
     * @param windStore
     *            if <code>null</code>, an {@link EmptyWindStore} will be used. Note that the {@link #windStore} field
     *            won't be serialized. A receiver of this operation will therefore always use an {@link EmptyWindStore}.
     * @param raceLogStore
     *            if <code>null</code>, an {@link EmptyRaceLogStore} will be used. Note that the {@link #raceLogStore} field
     *            won't be serialized. A receiver of this operation will therefore always use an {@link EmptyRaceLogStore}.
     */
    public CreateTrackedRace(RegattaAndRaceIdentifier raceIdentifier, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed, 
            RaceLogStore raceLogStore) {
        super(raceIdentifier);
        this.windStore = windStore;
        this.raceLogStore = raceLogStore;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
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

    @Override
    public TrackedRace internalApplyTo(RacingEventService toState) {
        return toState.createTrackedRace(getRaceIdentifier(), windStore == null ? EmptyWindStore.INSTANCE : windStore,
                delayToLiveInMillis, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                raceLogStore == null ? EmptyRaceLogStore.INSTANCE : raceLogStore);
    }

}
