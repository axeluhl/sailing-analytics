package com.sap.sailing.dashboards.gwt.client.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardHeaderResources extends ClientBundle {

    public static final DashboardHeaderResources INSTANCE =  GWT.create(DashboardHeaderResources.class);

    @Source("com/sap/sailing/dashboards/gwt/client/images/logo_sap.png")
    ImageResource sapLogo();
    
    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "DashboardHeader.gss"})
    DashboardHeaderGSS gss();

    public interface DashboardHeaderGSS extends CssResource {
        String sap_logo();
        String event_race_container();
        String event();
        String race();
    }
}
