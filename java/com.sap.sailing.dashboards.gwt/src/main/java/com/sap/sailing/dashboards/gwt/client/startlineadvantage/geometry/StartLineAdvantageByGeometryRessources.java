package com.sap.sailing.dashboards.gwt.client.startlineadvantage.geometry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface StartLineAdvantageByGeometryRessources extends ClientBundle {
    
    public static final StartLineAdvantageByGeometryRessources INSTANCE = GWT.create(StartLineAdvantageByGeometryRessources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/resources/theme/theme.gss", "StartLineAdvantageByGeometry.gss"})
    StartLineAdvantageByGeometryGss gss();
    
    @Shared
    public interface StartLineAdvantageByGeometryGss extends CssResource {
        public String startLineAdvantageComponent_liveAveragePanel();

        public String startLineAdvantageComponent_header();

        public String startLineAdvantageComponent_livePanel();

        public String startLineAdvantageComponent_averagePanel();

        public String startLineAdvantageComponent_middleLine();

        public String startLineAdvantageComponent_wrapper();
    }
}
