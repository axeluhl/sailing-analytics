package com.sap.sailing.dashboards.gwt.shared.dispatch;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardAction<R extends Result> extends Action<R, DashboardDispatchContext> {

    @GwtIncompatible
    R execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException;

}