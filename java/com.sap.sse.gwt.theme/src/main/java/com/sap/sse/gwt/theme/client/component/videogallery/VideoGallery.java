package com.sap.sse.gwt.theme.client.component.videogallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class VideoGallery extends Composite {
    private static VideoGalleryUiBinder uiBinder = GWT.create(VideoGalleryUiBinder.class);

    interface VideoGalleryUiBinder extends UiBinder<Widget, VideoGallery> {
    }
    
    private final VideoGalleryData videoGalleryData;
   
    @UiField DivElement galleryTitle;

    public VideoGallery(VideoGalleryData data) {
        this.videoGalleryData = data;
        
        VideoGalleryResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        galleryTitle.setInnerText(this.videoGalleryData.getName() != null ? this.videoGalleryData.getName() : "");
    }
}
