package com.sap.sailing.dashboards.gwt.client;

import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.sap.sailing.dashboards.gwt.client.dashboardpanel.DashboardPanel;
import com.sap.sailing.dashboards.gwt.client.dataretriever.WindBotDataRetriever;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.AbstractEntryPoint;

/**
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardEntryPoint extends AbstractEntryPoint<StringMessages> {

    @Override
    public void doOnModuleLoad() {
        applyMGWTSettings();
        DashboardClientFactory dashboardClientFactory = new DashboardClientFactoryImpl();
        DashboardPanel rootPanel = new DashboardPanel(dashboardClientFactory, this);
        RootLayoutPanel.get().add(rootPanel);
        WindBotDataRetriever windBotDataRetriever = new WindBotDataRetriever(dashboardClientFactory);
        windBotDataRetriever.addNumberOfWindBotsChangeListeners(rootPanel);
    }

    private void applyMGWTSettings() {
        MGWTStyle.injectStyleSheet("RibDashboard.css");
        ViewPort viewPort = new MGWTSettings.ViewPort();
        viewPort.setUserScaleAble(false).setMinimumScale(1.0).setMinimumScale(1.0).setMaximumScale(1.0);
        MGWTSettings settings = new MGWTSettings();
        settings.setViewPort(viewPort);
        MGWT.applySettings(settings);
    }

    @Override
    protected StringMessages createStringMessages() {
        return StringMessages.INSTANCE;
    }
}