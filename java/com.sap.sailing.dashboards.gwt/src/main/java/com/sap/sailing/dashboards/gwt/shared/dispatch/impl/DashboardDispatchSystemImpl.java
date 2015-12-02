package com.sap.sailing.dashboards.gwt.shared.dispatch.impl;

import com.sap.sailing.dashboards.gwt.client.RemoteServiceMappingConstants;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchContext;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;
import com.sap.sailing.gwt.dispatch.client.impl.DispatchSystemImpl;

public class DashboardDispatchSystemImpl extends DispatchSystemImpl<DashboardDispatchContext> implements DashboardDispatchSystem {

    public DashboardDispatchSystemImpl() {
        super(RemoteServiceMappingConstants.ribdashboardDispatchServiceRemotePath);
    }
}
