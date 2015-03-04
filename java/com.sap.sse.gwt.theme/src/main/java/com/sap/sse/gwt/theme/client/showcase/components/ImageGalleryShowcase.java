package com.sap.sse.gwt.theme.client.showcase.components;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageDescriptor;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGallery;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGalleryData;

public class ImageGalleryShowcase extends Composite {

    private static ImageGalleryShowcaseUiBinder uiBinder = GWT.create(ImageGalleryShowcaseUiBinder.class);

    interface ImageGalleryShowcaseUiBinder extends UiBinder<Widget, ImageGalleryShowcase> {
    }

    @UiField(provided=true)
    ImageGallery imageGallery;

    public ImageGalleryShowcase() {
        List<ImageDescriptor> images = new ArrayList<ImageDescriptor>(); 
        for(int i = 0; i < 10; i++) {
            ImageDescriptor desc = new ImageDescriptor("http://dummyimage.com/300x300/000/fff");
            desc.setTitle("Image " + (i+1));
            images.add(desc);
        }
        
        imageGallery = new ImageGallery(new ImageGalleryData("My image gallery", images));
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}
