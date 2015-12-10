package com.sap.sailing.gwt.dispatch.client;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;

public interface Action<R extends Result, CTX extends DispatchContext> extends IsSerializable {

    @GwtIncompatible
    R execute(CTX ctx) throws DispatchException;

}