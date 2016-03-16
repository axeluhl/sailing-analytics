package com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.course;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface StartlineAdvantageByGeometryWidgetRessources extends ClientBundle {
    
    public static final StartlineAdvantageByGeometryWidgetRessources INSTANCE = GWT.create(StartlineAdvantageByGeometryWidgetRessources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "StartlineAdvantageByGeometryWidget.gss"})
    StartLineAdvantageByGeometryGss gss();
    
    @Shared
    public interface StartLineAdvantageByGeometryGss extends CssResource {
        String start_line_advantage_by_geometry_widget();
        String advantage_maximum_live_average();
    }
}
