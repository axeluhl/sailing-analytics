package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class MarkedAsyncCallback<T> implements AsyncCallback<T> {
    
    public MarkedAsyncCallback() {
        PendingAjaxCallMarker.incrementPendingAjaxCalls();
    }
    
    @Override
    public final void onFailure(Throwable cause) {
        try {
            handleFailure(cause);
        } finally {
            //if(DebugInfo.isDebugIdEnabled())
            PendingAjaxCallMarker.decrementPendingAjaxCalls();
        }
    }

    @Override
    public final void onSuccess(T result) {
        try {
            handleSuccess(result);
        } finally {
            //if(DebugInfo.isDebugIdEnabled())
            PendingAjaxCallMarker.decrementPendingAjaxCalls();
        }
    }

    protected abstract void handleFailure(Throwable cause);
    
    protected abstract void handleSuccess(T result);
}
