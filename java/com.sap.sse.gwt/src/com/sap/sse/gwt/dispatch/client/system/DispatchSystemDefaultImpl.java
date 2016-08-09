package com.sap.sse.gwt.dispatch.client.system;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.system.batching.AutomaticBatchingDispatch;
import com.sap.sse.gwt.dispatch.client.system.caching.CachingDispatch;
import com.sap.sse.gwt.dispatch.client.transport.DefaultTransport;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * Base implementation of a client side dispatch executor.
 *
 * The dispatch system implements caching {@link CachingDispatch} and automatic batching
 * {@link AutomaticBatchingDispatch}.
 * 
 * The underlying transport is the GWT RPC implementation.
 * 
 * @param <CTX>
 */
public abstract class DispatchSystemDefaultImpl<CTX extends DispatchContext> implements DispatchSystemAsync<CTX>,
        ProvidesServerTime {
    private final Logger LOG = Logger.getLogger(DispatchSystemDefaultImpl.class.getName());
    private final DefaultTransport<CTX> simpleDispatch;
    private final DispatchSystemAsync<CTX> dispatch;

    public DispatchSystemDefaultImpl(String dispatchRPCPath) {
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
    public DispatchSystemDefaultImpl(String dispatchRPCPath, boolean processResultsScheduled) {
        simpleDispatch = new DefaultTransport<CTX>(dispatchRPCPath);
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
                    DispatchException sde = (DispatchException) caught;
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
