package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface StartAnalysisWidgetResources extends ClientBundle {

    public static final StartAnalysisWidgetResources INSTANCE =  GWT.create(StartAnalysisWidgetResources.class);
    
    @Source("com/sap/sailing/dashboards/gwt/client/images/left.png")
    ImageResource left();

    @Source("com/sap/sailing/dashboards/gwt/client/images/right.png")
    ImageResource right();
    
    @Source("com/sap/sailing/dashboards/gwt/client/images/left_disabled.png")
    ImageResource leftdisabled();

    @Source("com/sap/sailing/dashboards/gwt/client/images/right_disabled.png")
    ImageResource rightdisabled();

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "StartAnalysisWidget.gss"})
    StartAnalysisGSS gss();

    public interface StartAnalysisGSS extends CssResource {
        String startanalysis();
        String panel_header();
        String current_start_panel();
        String controll_header_button();
        String controll_header_button_image();
        String controll_header_button_left();
        String controll_header_button_right();
        String card_container();
        String card();
        String card_map_container();
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