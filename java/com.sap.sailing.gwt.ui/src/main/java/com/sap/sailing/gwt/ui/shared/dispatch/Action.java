package com.sap.sailing.gwt.ui.shared.dispatch;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;

public interface Action<R extends Result> extends IsSerializable {

    @GwtIncompatible
    R execute(DispatchContext ctx) throws DispatchException;

}