package com.sap.sse.gwt.theme.client.showcase.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.videogallery.VideoGallery;

public class VideoGalleryShowcase extends Composite {

    private static VideoGalleryShowcaseUiBinder uiBinder = GWT.create(VideoGalleryShowcaseUiBinder.class);

    interface VideoGalleryShowcaseUiBinder extends UiBinder<Widget, VideoGalleryShowcase> {
    }

    @UiField
    VideoGallery videoGallery;

    public VideoGalleryShowcase() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
