package com.sap.sailing.gwt.home.communication;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * Extended {@link Action} interface, which requires a {@link SailingDispatchContext} for server-side execution.
 * 
 * @param <R>
 *            {@link Result} type provided by the actions {@link #execute(SailingDispatchContext)} method
 */
public interface SailingAction<R extends Result> extends Action<R, SailingDispatchContext> {

    @GwtIncompatible
    R execute(SailingDispatchContext ctx) throws DispatchException;
}