package com.sap.sailing.dashboards.gwt.client.windchart.compass;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource;

public interface LocationPointerCompassRessources extends ClientBundle {
    
    public static final LocationPointerCompassRessources INSTANCE = GWT.create(LocationPointerCompassRessources.class);

    @Source("com/sap/sailing/dashboards/gwt/client/images/compass.png")
    ImageResource compass();
    
    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "LocationPointerCompass.gss"})
    LocationPointerCompassGSS gss();

    @Shared
    public interface LocationPointerCompassGSS extends CssResource {
        public String compass();
        public String needle();
        public String angleToLocationLabel();
        public String distanceToLocationLabel();
    }
}
