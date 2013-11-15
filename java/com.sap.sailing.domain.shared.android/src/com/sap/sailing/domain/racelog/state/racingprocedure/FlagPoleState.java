package com.sap.sailing.domain.racelog.state.racingprocedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;

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
    
    public List<FlagPole> computeUpcomingChanges() {
        if (!hasNextState()) {
            return Collections.emptyList();
        }
        
        List<FlagPole> changes = new ArrayList<FlagPole>();
        // TODO: compute upcoming changes
        changes.add(new FlagPole(Flags.NOVEMBER, false));
        return changes;
    }

    public static boolean describesSameState(FlagPoleState left, FlagPoleState right) {
        if (left.hasNextState() != right.hasNextState()) {
            return false;
        }
        
        if (left.getCurrentState().size() != right.getCurrentState().size()) {
            return false;
        }
        
        if (describesSamePoles(left.getCurrentState(), right.getCurrentState())) {
            if (left.hasNextState()) {
                
                return left.getNextStateValidFrom().compareTo(right.getNextStateValidFrom()) == 0 &&
                        describesSamePoles(left.getNextState(), right.getNextState());
            }
            return true;
        }
        return false;
    }

    private static boolean describesSamePoles(Collection<FlagPole> leftPoles, Collection<FlagPole> rightPoles) {
        for (int i = 0; i < leftPoles.size(); i++) {
            FlagPole leftPole = Util.get(leftPoles, i);
            FlagPole rightPole = Util.get(rightPoles, i);
            if (leftPole.getUpperFlag() != rightPole.getUpperFlag() ||
                    leftPole.getLowerFlag() != rightPole.getLowerFlag() ||
                    leftPole.isDisplayed() != rightPole.isDisplayed()) {
                return false;
            }
        }
        return true;
    }
    

}
