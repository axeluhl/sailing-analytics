package com.sap.sse.gwt.theme.client.component.imagegallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ImageGalleryResources extends ClientBundle {
    public static final ImageGalleryResources INSTANCE = GWT.create(ImageGalleryResources.class);

    @Source("ImageGallery.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String sampleCssClass();
    }
}
