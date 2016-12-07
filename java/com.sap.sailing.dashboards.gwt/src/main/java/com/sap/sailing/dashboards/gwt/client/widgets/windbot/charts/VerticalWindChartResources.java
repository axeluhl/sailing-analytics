package com.sap.sailing.dashboards.gwt.client.widgets.windbot.charts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface VerticalWindChartResources extends ClientBundle {

    public static final VerticalWindChartResources INSTANCE =  GWT.create(VerticalWindChartResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "VerticalWindChart.gss"})
    VerticalWindChartGSS gss();

    public interface VerticalWindChartGSS extends CssResource {
        String verical_wind_chart();
        String chart();
        String click_area();
        String click_hint();
        String loading_hint();
        String loading_hint_text();
        String click_hint_minutes();
    }
}
