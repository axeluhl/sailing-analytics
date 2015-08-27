package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sse.gwt.client.media.ImageDTO;

public class ImageGalleryItem extends UIObject {

    private static ImageGalleryItemUiBinder uiBinder = GWT.create(ImageGalleryItemUiBinder.class);
    
    interface ImageGalleryItemUiBinder extends UiBinder<Element, ImageGalleryItem> {
    }
    
    @UiField ImageElement imageUi;
    
    public ImageGalleryItem(ImageDTO image) {
        setElement(uiBinder.createAndBindUi(this));
        imageUi.setSrc(image.getSourceRef());
    }

}
