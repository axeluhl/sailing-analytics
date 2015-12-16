package com.sap.sailing.dashboards.gwt.client.resources;

/**
 * @author Alexander Ries (D062114)
 *
 */
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
 
public interface DashboardResources extends ClientBundle {
    
    public static final DashboardResources INSTANCE =  GWT.create(DashboardResources.class);
    
    @Source({"theme/theme.gss", "Ribstyle.gss"})
    DashboardGSS style();

    public interface DashboardGSS extends CssResource {
        String bar();
        String page();
        String collumn();
        String startanalysiscollumn();
        String collumnthree();
        String logo();
        String right();
        String left();
        String startline_advantage_by_wind_container();
        String startline_advantage_by_geometry_container();
        String windcharthint();
        String windloadinghint();
        String windloadinghinttext();
    }
}
