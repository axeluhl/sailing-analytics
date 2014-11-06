package com.sap.sailing.dashboards.gwt.client.windchart.compass;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface LocationPointerCompassRessources extends ClientBundle {
    public static final LocationPointerCompassRessources INSTANCE = GWT.create(LocationPointerCompassRessources.class);

    @Source("LocationPointerCompass.css")
    LocalCss css();

    @Shared
    public interface LocalCss extends CssResource {

        public String locationPointerCompass();

        public String locationPointerCompass_needle();

        public String locationPointerCompass_angleToLocationLabel();

        public String locationPointerCompass_distanceToLocationLabel();
    }
}
