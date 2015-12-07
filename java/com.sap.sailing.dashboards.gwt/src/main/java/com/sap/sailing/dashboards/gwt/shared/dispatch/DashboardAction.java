package com.sap.sailing.dashboards.gwt.shared.dispatch;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.Result;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardAction<R extends Result> extends Action<R, DashboardDispatchContext> {

    @GwtIncompatible
    R execute(DashboardDispatchContext dashboardDispatchContext) throws DispatchException;

}