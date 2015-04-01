package com.sap.sailing.gwt.ui.server.dispatch;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public interface Handler<R extends Result, A extends Action<R>> {
    Class<A> getType();

    R execute(A action, DispatchContext context);
}