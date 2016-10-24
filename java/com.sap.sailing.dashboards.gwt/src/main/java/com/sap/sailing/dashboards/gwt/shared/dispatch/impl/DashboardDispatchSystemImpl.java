package com.sap.sailing.dashboards.gwt.shared.dispatch.impl;

import com.sap.sailing.dashboards.gwt.client.RemoteServiceMappingConstants;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;
import com.sap.sse.gwt.dispatch.client.system.DispatchSystemDefaultImpl;

public class DashboardDispatchSystemImpl extends DispatchSystemDefaultImpl<DashboardDispatchContext> implements DashboardDispatchSystem {

    public DashboardDispatchSystemImpl() {
        super(RemoteServiceMappingConstants.ribdashboardDispatchServiceRemotePath, true);
    }
}
