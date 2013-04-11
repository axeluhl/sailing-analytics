package com.sap.sailing.gwt.ui.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * @author c5163874
 * A executor class making the actual remote call for an AsyncAction.
 * The class is managing the number of executed actions in order to prevent a server overload.
 * If the amount of actions to be executed exceeds a defined threshold the execution of those actions will be dropped.
 */
public class AsyncActionsExecutor {
    private Map<String, AsyncAction<?>> lastRequestedActions;
    private int numPendingCalls;
    private final int maxPendingCalls;
    private final int maxPendingCallsPerType;
    private Map<String, Integer> actionsPerType;

    public AsyncActionsExecutor() {
        numPendingCalls = 0;
        maxPendingCalls = 6;
        maxPendingCallsPerType = 4;
        lastRequestedActions = new HashMap<String, AsyncAction<?>>();
        actionsPerType = new HashMap<String, Integer>();
    }

    public <T> void execute(final AsyncAction<T> action) {
        Integer numActionsOfType = actionsPerType.get(action.getType());
        if (numPendingCalls >= maxPendingCalls || (numActionsOfType != null && numActionsOfType >= maxPendingCallsPerType)) {
            GWT.log("Drop action : " + action.getType());
            // don't put the call into the execution queue, but save it as the last one of each type
            lastRequestedActions.put(action.getType(), action);
            return;
        }
        action.execute(this);
        if (numActionsOfType == null) {
            numActionsOfType = 0;
        }
        actionsPerType.put(action.getType(), numActionsOfType+1);
        numPendingCalls++;
        GWT.log("Execute action: " + action.getType());
        GWT.log("Pending actions counter: " + numPendingCalls);
    }

    protected void callCompleted(String type) {
        Integer numActionsPerType = actionsPerType.get(type);
        if (numActionsPerType != null && numActionsPerType > 0) {
            actionsPerType.put(type, numActionsPerType-1);
        }
        numPendingCalls--;
        checkForEmptyCallQueue(type);
    }

    private void checkForEmptyCallQueue(String type) {
        Integer numActionsPerType = actionsPerType.get(type);
        if (numActionsPerType != null && numActionsPerType < maxPendingCallsPerType && lastRequestedActions.containsKey(type)) {
            AsyncAction<?> lastRequestedAction = lastRequestedActions.remove(type);
            GWT.log("Set back last action : " + lastRequestedAction.getType());
            execute(lastRequestedAction);
        }
    }
}
