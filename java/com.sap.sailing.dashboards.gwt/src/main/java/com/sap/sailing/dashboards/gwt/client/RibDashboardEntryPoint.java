package com.sap.sailing.dashboards.gwt.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.MGWTSettings.StatusBarStyle;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardEntryPoint implements EntryPoint {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RibDashboardEntryPoint.class.getName());
    private static final int DASHBOARD_RERFRESH_INTERVAL = 5000;

    @Override
    public void onModuleLoad() {
        applyMGWTSettings();
        RibDashboardPanel ribDashboardPanel = new RibDashboardPanel();
        RootLayoutPanel.get().add(ribDashboardPanel);
        WindBotDataRetriever windBotDataRetriever = new WindBotDataRetriever();
        windBotDataRetriever.addNumberOfWindBotsChangeListeners(ribDashboardPanel);
        RibDashboardDataRetriever.getInstance().addRaceSelectionChangeListener(windBotDataRetriever);
        Timer dashboardTimer = new Timer(PlayModes.Live);
        dashboardTimer.setRefreshInterval(DASHBOARD_RERFRESH_INTERVAL);
        dashboardTimer.addTimeListener(windBotDataRetriever);
        dashboardTimer.addTimeListener(RibDashboardDataRetriever.getInstance());
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
}