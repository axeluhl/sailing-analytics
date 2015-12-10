package com.sap.sailing.gwt.home.communication;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.Result;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;

public interface SailingAction<R extends Result> extends Action<R, SailingDispatchContext> {

    @GwtIncompatible
    R execute(SailingDispatchContext ctx) throws DispatchException;

}