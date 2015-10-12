package com.sap.sailing.gwt.home.mobile.places.event.media;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.imagegallery.ImageGallery;
import com.sap.sailing.gwt.home.mobile.partials.videogallery.VideoGallery;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingVideoDTO;

public class MediaViewImpl extends AbstractEventView<MediaView.Presenter> implements MediaView {

    private static MediaViewImplUiBinder uiBinder = GWT.create(MediaViewImplUiBinder.class);

    interface MediaViewImplUiBinder extends UiBinder<Widget, MediaViewImpl> {
    }
    
    @UiField Label noContentInfoUi;
    @UiField VideoGallery videoGalleryUi;
    @UiField ImageGallery imageGalleryUi;

    public MediaViewImpl(MediaView.Presenter presenter) {
        super(presenter, false, true);
        setViewContent(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setMedia(Collection<SailingVideoDTO> videos, Collection<SailingImageDTO> images) {
        noContentInfoUi.setVisible(videos.isEmpty() && images.isEmpty());
        videoGalleryUi.setVideos(videos);
        videoGalleryUi.setVisible(!videos.isEmpty());
        imageGalleryUi.setImages(images);
        imageGalleryUi.setVisible(!images.isEmpty());
    }
    
}
