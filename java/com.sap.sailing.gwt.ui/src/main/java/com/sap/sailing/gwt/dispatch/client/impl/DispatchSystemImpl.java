package com.sap.sailing.gwt.dispatch.client.impl;

import java.util.Date;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.DispatchAsync;
import com.sap.sailing.gwt.dispatch.client.DispatchContext;
import com.sap.sailing.gwt.dispatch.client.Result;
import com.sap.sailing.gwt.dispatch.client.batching.AutomaticBatchingDispatch;
import com.sap.sailing.gwt.dispatch.client.caching.CachingDispatch;
import com.sap.sailing.gwt.dispatch.client.exceptions.ServerDispatchException;
import com.sap.sailing.gwt.dispatch.client.rpcimpl.SimpleDispatch;

public class DispatchSystemImpl<CTX extends DispatchContext> implements DispatchSystem<CTX> {
    
    private final SimpleDispatch<CTX> simpleDispatch;
    private final DispatchAsync<CTX> dispatch;

    public DispatchSystemImpl(String dispatchRPCPath) {
        this(dispatchRPCPath, false);
    }
    
    public DispatchSystemImpl(String dispatchRPCPath, boolean processResultsScheduled) {
        simpleDispatch = new SimpleDispatch<CTX>(dispatchRPCPath);
        dispatch = new CachingDispatch<CTX>(new AutomaticBatchingDispatch<CTX>(
                simpleDispatch, processResultsScheduled));
    }
    
    @Override
    public <R extends Result, A extends Action<R, CTX>> void execute(final A action, final AsyncCallback<R> callback) {
        // TODO: client side execution time logging
        AsyncCallback<R> wrappedCallback = new AsyncCallback<R>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof ServerDispatchException) {
                    ServerDispatchException sde = (ServerDispatchException) caught;
                    // TODO: browser console log
                    GWT.log("Server exception with id: " + sde.getUuid());
                }
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(R result) {
                callback.onSuccess(result);
            }
        };
        dispatch.execute(action, wrappedCallback);
    }

    @Override
    public Date getCurrentServerTime() {
        return new Date(System.currentTimeMillis() + simpleDispatch.getClientServerOffset());
    }
}
