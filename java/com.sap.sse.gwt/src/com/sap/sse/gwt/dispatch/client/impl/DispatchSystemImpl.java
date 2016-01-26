package com.sap.sse.gwt.dispatch.client.impl;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.Action;
import com.sap.sse.gwt.dispatch.client.DispatchSystemAsync;
import com.sap.sse.gwt.dispatch.client.DispatchContext;
import com.sap.sse.gwt.dispatch.client.Result;
import com.sap.sse.gwt.dispatch.client.batching.AutomaticBatchingDispatch;
import com.sap.sse.gwt.dispatch.client.caching.CachingDispatch;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.gwt.dispatch.client.exceptions.ServerDispatchException;
import com.sap.sse.gwt.dispatch.client.rpcimpl.SimpleDispatch;

/**
 * Base implementation of a client side dispatch executor.
 *
 * The dispatch system implements caching {@link CachingDispatch} and automatic batching
 * {@link AutomaticBatchingDispatch}.
 * 
 * @param <CTX>
 */
public abstract class DispatchSystemImpl<CTX extends DispatchContext> implements DispatchSystem<CTX> {
    private final Logger LOG = Logger.getLogger(DispatchSystemImpl.class.getName());
    private final SimpleDispatch<CTX> simpleDispatch;
    private final DispatchSystemAsync<CTX> dispatch;

    public DispatchSystemImpl(String dispatchRPCPath) {
        this(dispatchRPCPath, false);
    }
    
    /**
     * 
     * Create a dispatch system
     * 
     * @param dispatchRPCPath
     *            rpc-servlet path
     * @param processResultsScheduled
     *            use a {@link Scheduler} to process the results
     */
    public DispatchSystemImpl(String dispatchRPCPath, boolean processResultsScheduled) {
        simpleDispatch = new SimpleDispatch<CTX>(dispatchRPCPath);
        dispatch = new CachingDispatch<CTX>(new AutomaticBatchingDispatch<CTX>(
                simpleDispatch, processResultsScheduled));
        LOG.finest("Started dispatch system for " + dispatchRPCPath);
    }
    
    @Override
    public <R extends Result, A extends Action<R, CTX>> void execute(final A action, final AsyncCallback<R> callback) {
        final AsyncCallback<R> wrappedCallback = new AsyncCallback<R>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof DispatchException) {
                    ServerDispatchException sde = (ServerDispatchException) caught;
                    LOG.log(Level.SEVERE, "Server exception with id: " + sde.getExceptionId());
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
