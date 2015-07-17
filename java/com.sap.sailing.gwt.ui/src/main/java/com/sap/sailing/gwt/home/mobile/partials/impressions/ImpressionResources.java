package com.sap.sailing.gwt.home.mobile.partials.impressions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ImpressionResources extends ClientBundle {
    public static final ImpressionResources INSTANCE = GWT.create(ImpressionResources.class);

    @Source("Impressions.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String impressionImage();
    }
}
