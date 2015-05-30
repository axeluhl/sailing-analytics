package com.sap.sailing.gwt.ui.shared.dispatch.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.RequestWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWrapper;

public interface DispatchRPC extends RemoteService {
    
    <R extends Result, A extends Action<R>> ResultWrapper<R> execute(RequestWrapper<R, A> action) throws DispatchException;

}
