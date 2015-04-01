package com.sap.sailing.gwt.home.client.shared.dispatch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPCAsync;
import com.sap.sse.gwt.client.mvp.ClientFactory;

public class SimpleDispatch implements DispatchAsync {
    
    // TODO: use CF
    private static final DispatchRPCAsync dispatchRPC = GWT.create(DispatchRPC.class);
    
    private final ClientFactory clientFactory;
    
    public SimpleDispatch(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        ((ServiceDefTarget) dispatchRPC).setServiceEntryPoint("/gwt/service/dispatch");
        
    }

    @Override
    public <R extends Result, A extends Action<R>> void execute(A action, AsyncCallback<R> callback) {
        dispatchRPC.execute(action, callback);
    }

}
