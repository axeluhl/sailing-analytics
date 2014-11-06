package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface StartLineAdvantageComponentRessources extends ClientBundle {
    public static final StartLineAdvantageComponentRessources INSTANCE = GWT.create(StartLineAdvantageComponentRessources.class);

    @Source("StartLineAdvantageComponent.css")
    LocalCss css();
    
    @Shared
    public interface LocalCss extends CssResource {
        public String startLineAdvantageComponent_liveAveragePanel();

        public String startLineAdvantageComponent_header();

        public String startLineAdvantageComponent_livePanel();

        public String startLineAdvantageComponent_averagePanel();

        public String startLineAdvantageComponent_middleLine();

        public String startLineAdvantageComponent_wrapper();
    }
}
