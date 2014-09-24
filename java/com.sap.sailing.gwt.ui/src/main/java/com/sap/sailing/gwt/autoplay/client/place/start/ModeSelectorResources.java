package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ModeSelectorResources extends ClientBundle {
    public static final ModeSelectorResources INSTANCE = GWT.create(ModeSelectorResources.class);

    @Source("com/sap/sailing/gwt/autoplay/client/place/start/ModeSelector.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
    }
}
