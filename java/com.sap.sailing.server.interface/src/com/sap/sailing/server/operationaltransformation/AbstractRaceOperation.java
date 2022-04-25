package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Util.Triple;

public abstract class AbstractRaceOperation<ResultType> extends AbstractRacingEventServiceOperation<ResultType> {
    private static final long serialVersionUID = -1162468486451355784L;
    private RegattaAndRaceIdentifier raceIdentifier;
    
    public AbstractRaceOperation(RegattaAndRaceIdentifier raceIdentifier) {
        super();
        this.raceIdentifier = raceIdentifier;
    }
    
    /**
     * The default for race operations is that their {@link #internalApplyTo(RacingEventService)} method already
     * replicates the operation's effects.
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }
    
    protected DynamicTrackedRace getTrackedRace(RacingEventService racingEventService) {
        // it's fair to not wait for the tracked race to arrive here because we're receiving a replication operation
        // and the synchronous race-creating operation must have been processed synchronously before this operation
        // could even have been received
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) racingEventService.getExistingTrackedRace(getRaceIdentifier());
        return trackedRace;
    }

    /**
     * Returns a {@link Triple} consisting of this operation's {@link #getClass() class}, its {@link #getRaceIdentifier() race identifier}
     * and a race-specific key component obtained through the {@link #getRaceSpecificKeyComponentForAsynchronousExecution()} method.
     */
    @Override
    public Object getKeyForAsynchronousExecution() {
        return new Triple<>(getClass(), raceIdentifier, getRaceSpecificKeyComponentForAsynchronousExecution());
    }
    
    /**
     * To simplify the implementation of the {@link #getKeyForAsynchronousExecution()} method, specializations can override
     * this method to provide a race-specific key for the operation that is equal to that of other operations of the same type
     * if the operations may mutually block each other's execution.<p>
     * 
     * For example, an operation to add a GPS fix to a competitor's track within a race only needs to return the competitor ID
     * from this method; operation type and race identifier are contributed into a triple returned by this class's
     * {@link #getKeyForAsynchronousExecution()} implementation by default.
     * 
     * @return this default implementation returns {@code null}.
     */
    protected Object getRaceSpecificKeyComponentForAsynchronousExecution() {
        return null;
    }

    @Override
    public String toString() {
        return super.toString()+" [raceIdentifier=" + raceIdentifier + "]";
    }
}
