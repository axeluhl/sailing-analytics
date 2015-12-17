package com.sap.sailing.dashboards.gwt.client.windchart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface WindBotComponentResources extends ClientBundle {

    public static final WindBotComponentResources INSTANCE =  GWT.create(WindBotComponentResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "WindBotComponent.gss"})
    WindBotComponentGss gss();

    public interface WindBotComponentGss extends CssResource {
        String wind_bot_component();
        String name_panel();
        String live_average_container();
        String left();
        String right();
        String windchart_container();
        String windchart();
        String compass_container();
    }
}