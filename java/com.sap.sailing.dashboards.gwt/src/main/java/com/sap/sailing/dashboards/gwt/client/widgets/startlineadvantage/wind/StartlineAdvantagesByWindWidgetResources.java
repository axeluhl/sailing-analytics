package com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.wind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface StartlineAdvantagesByWindWidgetResources extends ClientBundle {

    public static final StartlineAdvantagesByWindWidgetResources INSTANCE =  GWT.create(StartlineAdvantagesByWindWidgetResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "StartlineAdvantagesByWindWidget.gss"})
    StartlineAdvantagesOnLineChartGSS gss();

    public interface StartlineAdvantagesOnLineChartGSS extends CssResource {
        String start_line_advantages_by_wind_component();
        String advantage_maximum_live_average();
        String advantages_on_line_chart_container();
    }
}
