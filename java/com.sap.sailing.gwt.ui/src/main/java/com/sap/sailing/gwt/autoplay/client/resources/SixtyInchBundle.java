package com.sap.sailing.gwt.autoplay.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SixtyInchBundle extends ClientBundle {

    @Source("css/60inch.css")
    SixtyInchStyles style();

    public interface SixtyInchStyles extends CssResource {
        String slideBackground();
    }
}
