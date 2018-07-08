package com.sap.sse.gwt.dispatch.client.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.ServiceRoutingProvider;
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
    private final DispatchSystemAsync<CTX> defaultDispatch;
    private final Map<String,DispatchSystemAsync<CTX>> routingRegistry = new HashMap<>();
    private final boolean processResultsScheduled;
    private final String dispatchRPCPath;
    
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
        this.dispatchRPCPath = dispatchRPCPath;
        this.processResultsScheduled = processResultsScheduled;
        simpleDispatch = new DefaultTransport<CTX>(dispatchRPCPath);
        defaultDispatch = new CachingDispatch<CTX>(new AutomaticBatchingDispatch<CTX>(simpleDispatch, processResultsScheduled));
        LOG.finest("Started default dispatch system for " + dispatchRPCPath);
    }
    
    private DispatchSystemAsync<CTX> createDispatchFor(String routing) {
        final StringBuilder destinationUrlBuilder = new StringBuilder(dispatchRPCPath);
        if (routing != null && !routing.isEmpty()) {
            if (!dispatchRPCPath.endsWith("/")) {
                destinationUrlBuilder.append("/");
            }
            destinationUrlBuilder.append(routing.startsWith("/") ? routing.substring(1) : routing);
        }
        DefaultTransport<CTX> transport = new DefaultTransport<CTX>(destinationUrlBuilder.toString());
        DispatchSystemAsync<CTX> dispatch = new CachingDispatch<CTX>(new AutomaticBatchingDispatch<CTX>(
                transport, processResultsScheduled));
        return dispatch;
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
        final DispatchSystemAsync<CTX> dispatchToUse;
        if (action instanceof ServiceRoutingProvider) {
            ServiceRoutingProvider providesDispatchRoutingKey = (ServiceRoutingProvider) action;
            String routingPath = providesDispatchRoutingKey.routingSuffixPath();
            
            dispatchToUse = routingRegistry.computeIfAbsent(routingPath, this::createDispatchFor);
            
            LOG.fine("Using routed dispatch for path "+ routingPath);
        } else {
            LOG.fine("Using default dispatch for "+ action.getClass().getName());
            dispatchToUse = defaultDispatch;
        }
        dispatchToUse.execute(action, wrappedCallback);
    }

    @Override
    public Date getCurrentServerTime() {
        return new Date(System.currentTimeMillis() + simpleDispatch.getClientServerOffset());
    }
}
