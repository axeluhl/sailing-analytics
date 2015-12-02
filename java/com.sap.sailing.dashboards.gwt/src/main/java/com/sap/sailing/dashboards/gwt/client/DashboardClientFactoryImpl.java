package com.sap.sailing.dashboards.gwt.client;

import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;
import com.sap.sailing.dashboards.gwt.shared.dispatch.impl.DashboardDispatchSystemImpl;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardClientFactoryImpl implements DashboardClientFactory{

    private final DashboardDispatchSystem dispatch = new DashboardDispatchSystemImpl();
    
    @Override
    public DashboardDispatchSystem getDispatch() {
        return dispatch;
    }

}
