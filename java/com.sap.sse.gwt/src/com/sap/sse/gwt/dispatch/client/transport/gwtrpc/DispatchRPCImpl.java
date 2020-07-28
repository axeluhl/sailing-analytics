package com.sap.sse.gwt.dispatch.client.transport.gwtrpc;

import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_REPLICA;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.client.system.DispatchSystemAsync;
import com.sap.sse.gwt.dispatch.client.system.batching.BatchAction;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.HasWriteAction;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * Simple dispatch implementation that uses GWT RPC internally to communicate with the server
 *
 * @param <CTX>
 */
public class DispatchRPCImpl<CTX extends DispatchContext> implements DispatchSystemAsync<CTX> {
    
    private final DispatchRPCAsync<CTX> dispatchReadRPC = GWT.create(DispatchRPC.class);
    private final DispatchRPCAsync<CTX> dispatchWriteRPC = GWT.create(DispatchRPC.class);

    private long clientServerOffset = 0;
    
    public DispatchRPCImpl(String dispatchRPCPath) {
        //((ServiceDefTarget) dispatchRPC).setServiceEntryPoint(dispatchRPCPath);
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) dispatchReadRPC, dispatchRPCPath, HEADER_FORWARD_TO_REPLICA);
        EntryPointHelper.registerASyncService((ServiceDefTarget) dispatchWriteRPC, dispatchRPCPath, HEADER_FORWARD_TO_MASTER);
        
    }

    @Override
    public <R extends Result, A extends Action<R, CTX>> void execute(A action, final AsyncCallback<R> callback) {
        final RequestWrapper<R, A, CTX> requestWrapper = new RequestWrapper<R, A, CTX>(action, LocaleInfo
                .getCurrentLocale()
                .getLocaleName());
        final long clientTimeOnRequestStart = System.currentTimeMillis();
        
        DispatchRPCAsync<CTX> dispatcher = dispatchReadRPC;
        if (action instanceof BatchAction) {
            @SuppressWarnings({ "rawtypes", "unchecked" }) // no typing is needed here
            List<Action> actions = ((BatchAction) action).getActions();
            if (actions != null) {
                for (Action<?,?> a : actions) {
                    if (a instanceof HasWriteAction) {
                        dispatcher = dispatchWriteRPC;
                        break;
                    }
                }
            }
        } else if (action instanceof HasWriteAction) {
            dispatcher = dispatchWriteRPC;
        }
        dispatcher.execute(requestWrapper, new AsyncCallback<ResultWrapper<R>>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResultWrapper<R> result) {
                long clientTimeOnRequestEnd = System.currentTimeMillis();
                long latency = (clientTimeOnRequestEnd - clientTimeOnRequestStart) / 2;
                long currentServerTime = result.getCurrentServerTime().getTime() + latency;
                clientServerOffset = currentServerTime - clientTimeOnRequestEnd;
                callback.onSuccess(result.getResult());
            }
        });
    }

    public long getClientServerOffset() {
        return clientServerOffset;
    }
}
