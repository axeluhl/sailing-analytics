package com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.wind.charts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface StartlineAdvantagesOnLineChartResources extends ClientBundle {

    public static final StartlineAdvantagesOnLineChartResources INSTANCE =  GWT.create(StartlineAdvantagesOnLineChartResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "StartlineAdvantagesOnLineChart.gss"})
    StartlineAdvantagesOnLineChartGSS gss();

    public interface StartlineAdvantagesOnLineChartGSS extends CssResource {
        String advantages_on_line_chart_container();
        String confidence_bar_container();
        String confidence_bar();
        String confidence_bar_header();
        String confidence_bar_label();
        String confidence_bar_min_label();
        String confidence_bar_max_label();
    }
}