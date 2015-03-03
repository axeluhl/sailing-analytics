package com.sap.sse.gwt.theme.client.component.videogallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface VideoGalleryResources extends ClientBundle {
    public static final VideoGalleryResources INSTANCE = GWT.create(VideoGalleryResources.class);

    @Source("VideoGallery.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String sampleCssClass();
    }
}
