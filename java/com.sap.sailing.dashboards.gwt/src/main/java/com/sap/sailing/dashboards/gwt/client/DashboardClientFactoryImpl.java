package com.sap.sailing.dashboards.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;
import com.sap.sailing.dashboards.gwt.shared.dispatch.impl.DashboardDispatchSystemImpl;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardClientFactoryImpl implements DashboardClientFactory{

    private final DashboardDispatchSystem dispatch;
    private final SailingServiceAsync sailingService;
    private final Timer dashboardFiveSecondsTimer;
    
    private static final int DASHBOARD_RERFRESH_INTERVAL = 5000;

    public DashboardClientFactoryImpl() {
        dispatch = new DashboardDispatchSystemImpl();
        sailingService = GWT.create(SailingService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.WEB_CONTEXT_PATH, RemoteServiceMappingConstants.sailingServiceRemotePath);
        dashboardFiveSecondsTimer = new Timer(PlayModes.Live);
        dashboardFiveSecondsTimer.setRefreshInterval(DASHBOARD_RERFRESH_INTERVAL);
    }
    
    @Override
    public DashboardDispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public SailingServiceAsync getSailingService() {
        return sailingService;
    }

    @Override
    public Timer getDashboardFiveSecondsTimer() {
        return dashboardFiveSecondsTimer;
    }

}
