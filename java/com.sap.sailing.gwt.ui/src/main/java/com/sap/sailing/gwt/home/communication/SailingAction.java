package com.sap.sailing.gwt.home.communication;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

public interface SailingAction<R extends Result> extends Action<R, SailingDispatchContext> {

    @GwtIncompatible
    R execute(SailingDispatchContext ctx) throws DispatchException;

}