package com.sap.sse.gwt.theme.client.showcase.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGallery;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGalleryData;

public class ImageGalleryShowcase extends Composite {

    private static ImageGalleryShowcaseUiBinder uiBinder = GWT.create(ImageGalleryShowcaseUiBinder.class);

    interface ImageGalleryShowcaseUiBinder extends UiBinder<Widget, ImageGalleryShowcase> {
    }

    @UiField(provided=true)
    ImageGallery imageGallery;

    public ImageGalleryShowcase() {
        imageGallery = new ImageGallery(new ImageGalleryData("My image gallery"));
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}
