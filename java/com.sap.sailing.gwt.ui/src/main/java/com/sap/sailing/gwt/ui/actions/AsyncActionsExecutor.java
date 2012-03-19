package com.sap.sailing.gwt.ui.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncActionsExecutor {
    private Map<String, AsyncAction<?>> lastRequestedActions;
    private int numPendingCalls;
    private final int maxPendingCalls;
    private final int maxPendingCallsPerType;
    private Map<String, Integer> actionsPerType;

    public AsyncActionsExecutor() {
        numPendingCalls = 0;
        maxPendingCalls = 6;
        maxPendingCallsPerType = 3;
        lastRequestedActions = new HashMap<String, AsyncAction<?>>();
        actionsPerType = new HashMap<String, Integer>();
    }

    public <T> void execute(final AsyncAction<T> action) {
        Integer numActionsOfType = actionsPerType.get(action.getType());
        if (numPendingCalls >= maxPendingCalls || (numActionsOfType != null && numActionsOfType > maxPendingCallsPerType)) {
            GWT.log("Drop action : " + action.getType());
            // don't put the call into the execution queue, but save it as the last one of each type
            lastRequestedActions.put(action.getType(), action);
            return;
        }

        // Wrap with action callback to hook into the call chain
        AsyncCallback<T> wrapper = new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
                String actionName = action.getType();
                GWT.log("Execution failure for action of type: " + actionName);
                AsyncCallback<T> callback = action.getCallback();
                callback.onFailure(caught);
                Integer numActionsPerType = actionsPerType.get(actionName);
                if (numActionsPerType != null && numActionsPerType > 0) {
                    actionsPerType.put(actionName, numActionsPerType-1);
                }
                numPendingCalls--;
                checkForEmptyCallQueue(actionName);
            }

            @Override
            public void onSuccess(T result) {
                String actionName = action.getType();
                GWT.log("Execution success for action of type: " + actionName);
                AsyncCallback<T> callback = action.getCallback();
                callback.onSuccess(result);
                Integer numActionsPerType = actionsPerType.get(actionName);
                if (numActionsPerType != null && numActionsPerType > 0) {
                    actionsPerType.put(actionName, numActionsPerType-1);
                }
                numPendingCalls--;
                checkForEmptyCallQueue(actionName);
            }
        };
        action.setWrapperCallback(wrapper);
        action.execute();

        if (numActionsOfType == null) {
            numActionsOfType = 0;
        }
        actionsPerType.put(action.getType(), numActionsOfType+1);
        numPendingCalls++;
        GWT.log("Execute action: " + action.getType());
        GWT.log("Pending actions counter: " + numPendingCalls);
    }

    private void checkForEmptyCallQueue(String actionName) {
        if (numPendingCalls == 0 && lastRequestedActions.containsKey(actionName)) {
            AsyncAction<?> lastRequestedAction = lastRequestedActions.get(actionName);
            GWT.log("Set back last action : " + lastRequestedAction.getType());
            execute(lastRequestedAction);
        }
    }
}
