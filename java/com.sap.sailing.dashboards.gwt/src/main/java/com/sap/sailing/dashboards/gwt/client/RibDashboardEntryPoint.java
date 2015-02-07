package com.sap.sailing.dashboards.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.MGWTSettings.StatusBarStyle;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardEntryPoint extends AbstractEntryPoint<StringMessages> {

    private static final int DASHBOARD_RERFRESH_INTERVAL = 5000;

    @Override
    public void doOnModuleLoad() {
        SailingServiceAsync sailingService = GWT.create(SailingService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.WEB_CONTEXT_PATH, RemoteServiceMappingConstants.sailingServiceRemotePath);
        
        RibDashboardServiceAsync ribdashboardService = GWT.create(RibDashboardService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) ribdashboardService, RemoteServiceMappingConstants.WEB_CONTEXT_PATH, RemoteServiceMappingConstants.ribdashboardServiceRemotePath);
        
        RibDashboardDataRetriever dataRetriever = RibDashboardDataRetriever.getInstance(ribdashboardService);

        applyMGWTSettings();
        RibDashboardPanel ribDashboardPanel = new RibDashboardPanel(ribdashboardService);
        RootLayoutPanel.get().add(ribDashboardPanel);
        WindBotDataRetriever windBotDataRetriever = new WindBotDataRetriever(sailingService);
        windBotDataRetriever.addNumberOfWindBotsChangeListeners(ribDashboardPanel);
        dataRetriever.addRaceSelectionChangeListener(windBotDataRetriever);
        Timer dashboardTimer = new Timer(PlayModes.Live);
        dashboardTimer.setRefreshInterval(DASHBOARD_RERFRESH_INTERVAL);
        dashboardTimer.addTimeListener(windBotDataRetriever);
        dashboardTimer.addTimeListener(dataRetriever);
    }

    private void applyMGWTSettings() {
        MGWTStyle.injectStyleSheet("RibDashboard.css");
        ViewPort viewPort = new MGWTSettings.ViewPort();
        viewPort.setUserScaleAble(false).setMinimumScale(1.0).setMinimumScale(1.0).setMaximumScale(1.0);
        MGWTSettings settings = new MGWTSettings();
        settings.setViewPort(viewPort);
        settings.setFullscreen(true);
        settings.setPreventScrolling(true);
        settings.setStatusBarStyle(StatusBarStyle.BLACK_TRANSLUCENT);
        MGWT.applySettings(settings);
    }

    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(com.sap.sailing.gwt.ui.client.StringMessages.class);
    }
}