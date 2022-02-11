package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface ImageGalleryResources extends SharedHomeResources {
    public static final ImageGalleryResources INSTANCE = GWT.create(ImageGalleryResources.class);

    @Source("ImageGallery.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String imagegallery();
        String imagegallery_column();
        String imagegallery_columnfirst();
        String imagegallery_columnsecond();
        String imagegallery_column_item();
    }
}
