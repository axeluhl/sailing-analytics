package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncActionsExecutor {
    private AsyncAction<?> lastRequestedAction;
    private int numPending;
    private final int maxPendingCalls;

    public AsyncActionsExecutor() {
        numPending = 0;
        maxPendingCalls = 5;
        lastRequestedAction = null;
    }

    public void execute(final AsyncAction<?> action) {
        if(numPending >= maxPendingCalls) {
            GWT.log("Drop action : " + action.getName());

            // don't put the call into the execution queue, but save it as the last one
            lastRequestedAction = action;
            return;
        }

        // Wrap with action callback to hook into the call chain
        AsyncCallback<?> wrapper = new AsyncCallback<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onFailure(Throwable caught) {
                    AsyncCallback<Object> callback = (AsyncCallback<Object>) action.getCallback();
                    callback.onFailure(caught);
                    numPending--;
                    finish();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object result) {
                AsyncCallback<Object> callback = (AsyncCallback<Object>) action.getCallback();
                callback.onSuccess(result);
                numPending--;
                finish();
            }
        };
        action.setWrapperCallback(wrapper);
        action.execute();
        
        numPending++;
        GWT.log("Execute action: " + action.getName());
        GWT.log("Pending actions counter: " + numPending);
    }

    private void finish() {
        if(numPending == 0 && lastRequestedAction != null) {
            execute(lastRequestedAction);
            GWT.log("Set back last action : " + lastRequestedAction.getName());
        }
    }
}
