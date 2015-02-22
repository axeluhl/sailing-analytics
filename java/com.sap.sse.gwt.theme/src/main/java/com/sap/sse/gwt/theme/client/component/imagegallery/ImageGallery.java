package com.sap.sse.gwt.theme.client.component.imagegallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ImageGallery extends Composite {
    private static ImageGalleryUiBinder uiBinder = GWT.create(ImageGalleryUiBinder.class);

    interface ImageGalleryUiBinder extends UiBinder<Widget, ImageGallery> {
    }
    
    public ImageGallery() {
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}
