package com.sap.sailing.dashboards.gwt.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.MGWTSettings.StatusBarStyle;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort;
import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;

/**
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardEntryPoint implements EntryPoint {

	private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
	private final String RIBDASHBOARD_WEB_CONTEXT_PATH = "dashboards";
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(RibDashboardEntryPoint.class.getName());

	@Override
	public void onModuleLoad() {
		initSailingService();
		applyMGWTSettings();
		initAndAddRibDashboardPanel();
	}

	private void initSailingService() {
		EntryPointHelper.registerASyncService(
				(ServiceDefTarget) sailingService,
				RIBDASHBOARD_WEB_CONTEXT_PATH,
				RemoteServiceMappingConstants.sailingServiceRemotePath);
	}

	private void applyMGWTSettings() {
		MGWTStyle.injectStyleSheet("RibDashboard.css");
		ViewPort viewPort = new MGWTSettings.ViewPort();
		viewPort.setUserScaleAble(false).setMinimumScale(1.0)
				.setMinimumScale(1.0).setMaximumScale(1.0);
		MGWTSettings settings = new MGWTSettings();
		settings.setViewPort(viewPort);
		settings.setFullscreen(true);
		settings.setPreventScrolling(true);
		settings.setStatusBarStyle(StatusBarStyle.BLACK_TRANSLUCENT);
		MGWT.applySettings(settings);
	}

	private void initAndAddRibDashboardPanel() {
		RibDashboardPanel root = new RibDashboardPanel();
		RootLayoutPanel.get().add(root);
	}
}