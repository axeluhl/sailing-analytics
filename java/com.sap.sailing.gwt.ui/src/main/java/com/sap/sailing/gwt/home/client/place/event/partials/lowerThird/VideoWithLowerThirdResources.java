package com.sap.sailing.gwt.home.client.place.event.partials.lowerThird;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface VideoWithLowerThirdResources extends ClientBundle {
    public static final VideoWithLowerThirdResources INSTANCE = GWT.create(VideoWithLowerThirdResources.class);

    @Source("VideoWithLowerThird.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String videoWithLowerThird();
        String video();
    }
}
