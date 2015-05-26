package com.sap.sailing.gwt.home.client.shared.dispatch;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.RequestWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPC;
import com.sap.sailing.gwt.ui.shared.dispatch.rpc.DispatchRPCAsync;

public class SimpleDispatch implements DispatchAsync {
    
    private static final DispatchRPCAsync dispatchRPC = GWT.create(DispatchRPC.class);
    
//    private final SailingClientFactory clientFactory;
    
    private Date lastServerTime;
    
    private Date lastClientTime;
    
    public SimpleDispatch(SailingClientFactory clientFactory) {
//        this.clientFactory = clientFactory;
        ((ServiceDefTarget) dispatchRPC).setServiceEntryPoint(RemoteServiceMappingConstants.dispatchServiceRemotePath);
        
    }

    @Override
    public <R extends Result, A extends Action<R>> void execute(A action, final AsyncCallback<R> callback) {
        dispatchRPC.execute(new RequestWrapper<R, A>(action), new AsyncCallback<ResultWrapper<R>>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ResultWrapper<R> result) {
                lastClientTime = new Date();
                lastServerTime = result.getCurrentServerTime();
                callback.onSuccess(result.getResult());
            }
        });
    }

    public Date getLastClientTime() {
        return lastClientTime;
    }
    
    public Date getLastServerTime() {
        return lastServerTime;
    }
}
