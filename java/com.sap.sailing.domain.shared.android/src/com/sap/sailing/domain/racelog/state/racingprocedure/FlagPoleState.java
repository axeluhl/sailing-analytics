package com.sap.sailing.domain.racelog.state.racingprocedure;

import java.util.Collection;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;

public class FlagPoleState {
    
    private final Collection<FlagPole> currentState;
    private final Collection<FlagPole> nextState;
    private final TimePoint nextStateValidFrom;
    private final boolean hasNextState;
    
    public FlagPoleState(Collection<FlagPole> currentState) {
        this(currentState, null, null, false);
    }
    
    public FlagPoleState(Collection<FlagPole> currentState, Collection<FlagPole> nextState, TimePoint nextStateValidFrom) {
        this(currentState, nextState, nextStateValidFrom, true);
    }
    
    private FlagPoleState(Collection<FlagPole> currentState, Collection<FlagPole> nextState, TimePoint nextStateValidFrom, boolean hasNextState) {
        this.currentState = currentState;
        this.nextState = nextState;
        this.nextStateValidFrom = nextStateValidFrom;
        this.hasNextState = hasNextState;
    }
    
    public Collection<FlagPole> getCurrentState() {
        return currentState;
    }
    public Collection<FlagPole> getNextState() {
        return nextState;
    }

    public TimePoint getNextStateValidFrom() {
        return nextStateValidFrom;
    }

    public boolean hasNextState() {
        return hasNextState;
    }
    

}
