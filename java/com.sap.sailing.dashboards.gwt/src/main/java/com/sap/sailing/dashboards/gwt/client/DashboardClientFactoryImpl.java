package com.sap.sailing.dashboards.gwt.client;

import com.sap.sailing.dashboards.gwt.shared.dispatch.DashboardDispatchSystem;
import com.sap.sailing.dashboards.gwt.shared.dispatch.impl.DashboardDispatchSystemImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.security.ui.client.DefaultWithSecurityImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserManagementWriteServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardClientFactoryImpl implements DashboardClientFactory{

    private final DashboardDispatchSystem dispatch;
    private final SailingServiceAsync sailingService;
    private final Timer dashboardFiveSecondsTimer;
    private final DefaultWithSecurityImpl securityProvider;
    
    private static final int DASHBOARD_RERFRESH_INTERVAL = 5000;

    public DashboardClientFactoryImpl() {
        dispatch = new DashboardDispatchSystemImpl();
        securityProvider = new DefaultWithSecurityImpl();
        sailingService = SailingServiceHelper.createSailingServiceInstance();
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

    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return securityProvider.getUserManagementService();
    }

    @Override
    public UserManagementWriteServiceAsync getUserManagementWriteService() {
        return securityProvider.getUserManagementWriteService();
    }

    @Override
    public UserService getUserService() {
        return securityProvider.getUserService();
    }

    @Override
    public SubscriptionServiceFactory getSubscriptionServiceFactory() {
        return securityProvider.getSubscriptionServiceFactory();
    }

}
