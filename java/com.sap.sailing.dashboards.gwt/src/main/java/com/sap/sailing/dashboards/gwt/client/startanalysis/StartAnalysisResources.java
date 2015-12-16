package com.sap.sailing.dashboards.gwt.client.startanalysis;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.ui.client.shared.racemap.CombinedWindPanelStyle;

interface StartAnalysisResources extends ClientBundle {

    public static final StartAnalysisResources INSTANCE =  GWT.create(StartAnalysisResources.class);
    
    @Source("com/sap/sailing/dashboards/gwt/client/images/windarrow.png")
    ImageResource combinedWindIcon();

    @Source({"com/sap/sailing/dashboards/gwt/client/resources/theme/theme.gss", "StartAnalysis.gss"})
    StartAnalysisGSS gss();
    
    @Source({"com/sap/sailing/dashboards/gwt/client/resources/theme/theme.gss", "StartAnalysisCombinedWindPanel.gss"})
    CombinedWindPanelStyle combinedWindPanelStyle();

    public interface StartAnalysisGSS extends CssResource {
        String startanalysis();
        String panel_header();
        String controll_header_button();
        String controll_header_button_left();
        String controll_header_button_right();
        String card_container();
        String card();
        String card_table();
        String card_wind_line_data_container();
        String card_data();
        String card_line_advantage();
        String card_race_time();
        String card_line_data_geo();
        String left_focus_panel();
        String right_focus_panel();
    }
}