package com.sap.sailing.gwt.home.shared.dispatch;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ServerDispatchException;

public class DispatchSystemImpl implements DispatchSystem {
    
    private final SimpleDispatch simpleDispatch = new SimpleDispatch(null);
    private final DispatchAsync dispatch = new AutomaticBatchingDispatch(simpleDispatch);

    @Override
    public <R extends Result, A extends Action<R>> void execute(final A action, final AsyncCallback<R> callback) {
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

}
