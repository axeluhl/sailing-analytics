package com.sap.sailing.dashboards.gwt.client.resources;

/**
 * @author Alexander Ries (D062114)
 *
 */
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
 
public interface DashboardResources extends ClientBundle {
    
    interface Style extends CssResource {
        // Your classes here
    }
    
    // We only need to source the theme file
    @Source({"colors.gss", "Ribstyle.css"})
    Style style();
}
