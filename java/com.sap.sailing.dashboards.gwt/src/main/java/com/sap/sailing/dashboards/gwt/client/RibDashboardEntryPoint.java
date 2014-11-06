package com.sap.sailing.dashboards.gwt.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.MGWTSettings.StatusBarStyle;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort;
import com.googlecode.mgwt.ui.client.MGWTStyle;

/**
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardEntryPoint implements EntryPoint{
    
    private static final Logger logger = Logger.getLogger(RibDashboardEntryPoint.class.getName());
    
    @Override
    public void onModuleLoad() {
        logger.log(Level.INFO,"ENTRY POINT CALLED");
        applyMGWTSettings();
        RibDashboardPanel root = new RibDashboardPanel();
        RootLayoutPanel.get().add(root);
    }
    
    private void applyMGWTSettings(){
        
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