package com.sap.sailing.gwt.ui.server.dispatch;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public interface DispatchContext {

    public <R extends Result, A extends Action<R>> R execute(A action) throws DispatchException;

}
