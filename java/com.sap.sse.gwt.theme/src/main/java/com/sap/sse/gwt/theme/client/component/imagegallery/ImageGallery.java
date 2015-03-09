package com.sap.sse.gwt.theme.client.component.imagegallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class ImageGallery extends Composite {
    private static ImageGalleryUiBinder uiBinder = GWT.create(ImageGalleryUiBinder.class);

    interface ImageGalleryUiBinder extends UiBinder<Widget, ImageGallery> {
    }
    
    private final ImageGalleryData imageGalleryData;
    
    @UiField DivElement titleUi;
    @UiField HTMLPanel imagesPanel;
    
    public ImageGallery(ImageGalleryData data) {
        this.imageGalleryData = data;
        
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        titleUi.setInnerText(this.imageGalleryData.getName() != null ? this.imageGalleryData.getName() : "");
        
        for(ImageDescriptor imageDesc: imageGalleryData.getImages()) {
            Image image = new Image(imageDesc);
            imagesPanel.add(image);
        }
    }
}
