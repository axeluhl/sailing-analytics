package com.sap.sailing.dashboards.gwt.client.widgets.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface DashboardWidgetHeaderAndNoDataMessageResources extends ClientBundle {

    public static final DashboardWidgetHeaderAndNoDataMessageResources INSTANCE =  GWT.create(DashboardWidgetHeaderAndNoDataMessageResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "DashboardWidgetHeaderAndNoDataMessage.gss"})
    DashboardWidgetHeaderAndNoDataMessageGSS gss();

    public interface DashboardWidgetHeaderAndNoDataMessageGSS extends CssResource {
        String dashboard_widget_header();
        String dashboard_widget_no_data_message_container();
        String dashboard_widget_no_data_message_header();
        String dashboard_widget_no_data_message();
    }
}