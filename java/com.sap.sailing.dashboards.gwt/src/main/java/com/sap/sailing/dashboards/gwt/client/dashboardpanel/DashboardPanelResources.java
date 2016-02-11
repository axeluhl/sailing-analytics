package com.sap.sailing.dashboards.gwt.client.dashboardpanel;

/**
 * @author Alexander Ries (D062114)
 *
 */
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
 
public interface DashboardPanelResources extends ClientBundle {
    
    public static final DashboardPanelResources INSTANCE =  GWT.create(DashboardPanelResources.class);
    
    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "DashboardPanel.gss"})
    DashboardGSS style();

    public interface DashboardGSS extends CssResource {
        String page();
        String header();
        String collumn();
        String startanalysiscollumn();
        String collumnthree();
        String right();
        String left();
        String startline_advantage_by_wind_container();
        String startline_advantage_by_geometry_container();
        String windcharthint();
        String event_logo();
    }
}
