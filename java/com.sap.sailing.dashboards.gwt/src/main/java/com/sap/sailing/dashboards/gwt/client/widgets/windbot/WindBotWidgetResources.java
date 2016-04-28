package com.sap.sailing.dashboards.gwt.client.widgets.windbot;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface WindBotWidgetResources extends ClientBundle {

    public static final WindBotWidgetResources INSTANCE =  GWT.create(WindBotWidgetResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "WindBotWidget.gss"})
    WindBotComponentGss gss();

    public interface WindBotComponentGss extends CssResource {
        String wind_bot_widget();
        String live_average_header();
        String live_average_container();
        String left();
        String right();
        String windchart_container();
        String windchart();
    }
}