package com.sap.sailing.dashboards.gwt.client;

import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardClientFactory{
    DashboardDispatchSystem getDispatch();
}
