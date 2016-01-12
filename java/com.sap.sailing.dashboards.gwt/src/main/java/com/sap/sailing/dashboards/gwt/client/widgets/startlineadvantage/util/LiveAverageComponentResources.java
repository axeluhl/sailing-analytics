package com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface LiveAverageComponentResources extends ClientBundle {

    public static final LiveAverageComponentResources INSTANCE =  GWT.create(LiveAverageComponentResources.class);
    
    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "LiveAverageComponent.gss"})
    LiveAverageComponentGss gss();
    
    interface LiveAverageComponentGss extends CssResource {
        String live_average_component();
        String header();
        String header_container();
        String live_container();
        String average_container();
        String middle_line();
        String panel_value_number();
        String panel_value_unit();
        String panel_value_unit_degrees();
        String value_panel();
        String label_text();
        String live_label_container();
        String average_label_container();
    }
}
