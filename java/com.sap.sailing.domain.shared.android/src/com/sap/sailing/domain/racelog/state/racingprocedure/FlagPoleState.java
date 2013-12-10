package com.sap.sailing.domain.racelog.state.racingprocedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;

/**
 * Allows you to query the current and state of displayed and remove flags.
 */
public class FlagPoleState {
    
    private final List<FlagPole> currentState;
    private final List<FlagPole> nextState;
    private final TimePoint nextStateValidFrom;
    private final boolean hasNextState;
    
    public FlagPoleState(List<FlagPole> currentState) {
        this(currentState, null, null, false);
    }
    
    public FlagPoleState(List<FlagPole> currentState, List<FlagPole> nextState, TimePoint nextStateValidFrom) {
        this(currentState, nextState, nextStateValidFrom, true);
    }
    
    private FlagPoleState(List<FlagPole> currentState, List<FlagPole> nextState, TimePoint nextStateValidFrom, boolean hasNextState) {
        this.currentState = currentState;
        this.nextState = nextState;
        this.nextStateValidFrom = nextStateValidFrom;
        this.hasNextState = hasNextState;
    }
    
    public List<FlagPole> getCurrentState() {
        return currentState;
    }
    public List<FlagPole> getNextState() {
        return nextState;
    }

    public TimePoint getNextStateValidFrom() {
        return nextStateValidFrom;
    }

    public boolean hasNextState() {
        return hasNextState;
    }
    
    public List<FlagPole> computeUpcomingChanges() {
        if (!hasNextState) {
            return Collections.emptyList();
        }
        
        List<FlagPole> changes = new ArrayList<FlagPole>();
        for (FlagPole nextPole : nextState) {
            boolean hasSamePoleInCurrentState = false;
            for (FlagPole currentPole : currentState) {
                if (nextPole.describesSame(currentPole)) {
                    hasSamePoleInCurrentState = true;
                    break;
                }
            }
            if (!hasSamePoleInCurrentState) {
                changes.add(nextPole);
            }
        }
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

    private static boolean describesSamePoles(List<FlagPole> leftPoles, List<FlagPole> rightPoles) {
        for (int i = 0; i < leftPoles.size(); i++) {
            FlagPole leftPole = leftPoles.get(i);
            FlagPole rightPole = rightPoles.get(i);
            if (!leftPole.describesSame(rightPole)) {
                return false;
            }
        }
        return true;
    }
    

}
