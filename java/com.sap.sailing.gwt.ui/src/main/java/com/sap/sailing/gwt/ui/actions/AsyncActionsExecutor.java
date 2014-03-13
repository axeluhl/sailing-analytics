package com.sap.sailing.gwt.ui.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;

/**
 * A executor class making the actual remote call for an {@link AsyncAction}. The class is managing the number of
 * executed actions in order to prevent a server overload. If the amount of actions to be executed exceeds a defined
 * threshold the execution of those actions will be dropped.
 * 
 * @author c5163874
 */
public class AsyncActionsExecutor {
    private class ExecutionJob<T> implements AsyncCallback<T> {
        private AsyncAction<T> action;
        private String category;
        private AsyncCallback<T> callback;
        
        public ExecutionJob(AsyncAction<T> action, String category, AsyncCallback<T> callback) {
            this.action = action;
            this.category = category;
            this.callback = callback;
        }
        
        public String getCategory() {
            return (this.category == null ? MarkedAsyncCallback.CATEGORY_GLOBAL : this.category);
        }
        
        public String getType() {
            return this.action.getClass().getName();
        }
        
        public void execute() {
            this.action.execute(new MarkedAsyncCallback<T>(this, getCategory()));
        }
        
        @Override
        public void onSuccess(T result) {
            try {
                GWT.log("Execution success for action of type: " + getType());
                this.callback.onSuccess(result);
            } finally {
                AsyncActionsExecutor.this.callCompleted(this);
            }
        }
        
        @Override
        public void onFailure(Throwable caught) {
            try {
                GWT.log("Execution failure for action of type: " + getType());
                this.callback.onFailure(caught);
            } finally {
                AsyncActionsExecutor.this.callCompleted(this);
            }
        }
    }
    
    private final int maxPendingCalls;
    private final int maxPendingCallsPerType;
    private int numPendingCalls = 0;
    private Map<String, Integer> actionsPerType = new HashMap<String, Integer>();
    private Map<String, ExecutionJob<?>> lastRequestedActions = new HashMap<String, ExecutionJob<?>>();
    
    
    public AsyncActionsExecutor() {
        this(6, 4);
    }
    
    public AsyncActionsExecutor(int maxPendingCalls, int maxPendingCallsPerType) {
        this.maxPendingCalls = maxPendingCalls;
        this.maxPendingCallsPerType = maxPendingCallsPerType;
    }
    
    public <T> void execute(AsyncAction<T> action, AsyncCallback<T> callback) {
        execute(action, MarkedAsyncCallback.CATEGORY_GLOBAL, callback);
    }
    
    public <T> void execute(AsyncAction<T> action, String category, AsyncCallback<T> callback) {
        execute(new ExecutionJob<T>(action, category, callback));
    }
    
    private void execute(ExecutionJob<?> job) {
        Integer numActionsOfType = actionsPerType.get(job.getType());
        
        if (numActionsOfType == null) {
            numActionsOfType = Integer.valueOf(0);
        }

        if (numPendingCalls >= maxPendingCalls || (numActionsOfType >= maxPendingCallsPerType)) {
            GWT.log("Drop action : " + job.getType());
            // don't put the call into the execution queue, but save it as the last one of each type
            lastRequestedActions.put(job.getType(), job);
            return;
        }
        
        actionsPerType.put(job.getType(), numActionsOfType + 1);
        numPendingCalls++;

        job.execute();
    }

    private void callCompleted(ExecutionJob<?> job) {
        String type = job.getType();
        Integer numActionsPerType = actionsPerType.get(type);
        if (numActionsPerType != null && numActionsPerType > 0) {
            actionsPerType.put(type, numActionsPerType - 1);
        }
        numPendingCalls--;
        checkForEmptyCallQueue(type);
    }

    private void checkForEmptyCallQueue(String type) {
        Integer numActionsPerType = actionsPerType.get(type);
        if (numActionsPerType != null && numActionsPerType < maxPendingCallsPerType && lastRequestedActions.containsKey(type)) {
            ExecutionJob<?> lastRequestedAction = lastRequestedActions.remove(type);
            GWT.log("Set back last action : " + lastRequestedAction.getType());
            execute(lastRequestedAction);
        }
    }
}
