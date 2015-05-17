package com.sap.sse.gwt.theme.client.component.imagegallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Image extends Composite {
    private static ImageUiBinder uiBinder = GWT.create(ImageUiBinder.class);

    interface ImageUiBinder extends UiBinder<Widget, Image> {
    }
    
    private final ImageDescriptor imageDescriptor;
    
    @UiField DivElement title;
    @UiField ImageElement image;
    
    public Image(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
        
        ImageGalleryResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        title.setInnerText(this.imageDescriptor.getTitle());
        image.setSrc(this.imageDescriptor.getImageURL());
    }
}
