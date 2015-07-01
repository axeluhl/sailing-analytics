package com.sap.sailing.gwt.home.shared.dispatch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
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
    
    private long clientServerOffset = 0;
    
    public SimpleDispatch(SailingClientFactory clientFactory) {
//        this.clientFactory = clientFactory;
        ((ServiceDefTarget) dispatchRPC).setServiceEntryPoint(RemoteServiceMappingConstants.dispatchServiceRemotePath);
        
    }

    @Override
    public <R extends Result, A extends Action<R>> void execute(A action, final AsyncCallback<R> callback) {
        RequestWrapper<R, A> requestWrapper = new RequestWrapper<R, A>(action, LocaleInfo.getCurrentLocale().getLocaleName());
        final long clientTimeOnRequestStart = System.currentTimeMillis();
        dispatchRPC.execute(requestWrapper, new AsyncCallback<ResultWrapper<R>>() {

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
