package com.sap.sailing.gwt.home.client.shared.dispatch;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public class DispatchSystemImpl implements DispatchSystem {
    
    private final SimpleDispatch simpleDispatch = new SimpleDispatch(null);
    private final DispatchAsync dispatch = new AutomaticBatchingDispatch(simpleDispatch);

    @Override
    public <R extends Result, A extends Action<R>> void execute(A action, AsyncCallback<R> callback) {
        dispatch.execute(action, callback);
    }

    @Override
    public Date getCurrentServerTime() {
        return new Date(new Date().getTime() - simpleDispatch.getLastClientTime().getTime() + simpleDispatch.getLastServerTime().getTime());
    }
}
