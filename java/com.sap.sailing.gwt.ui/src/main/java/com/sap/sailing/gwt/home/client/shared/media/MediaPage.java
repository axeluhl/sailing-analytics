package com.sap.sailing.gwt.home.client.shared.media;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaEntryDTO;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageDescriptor;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGallery;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGalleryData;
import com.sap.sse.gwt.theme.client.component.videogallery.VideoDescriptor;
import com.sap.sse.gwt.theme.client.component.videogallery.VideoGallery;
import com.sap.sse.gwt.theme.client.component.videogallery.VideoGalleryData;

public class MediaPage extends Composite {

    private static MediaPageUiBinder uiBinder = GWT.create(MediaPageUiBinder.class);

    interface MediaPageUiBinder extends UiBinder<Widget, MediaPage> {
    }
    
    @UiField ImageGallery photos;
    
    @UiField VideoGallery videos;
    @UiField HeadingElement noContent;

    public MediaPage() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setMedia(MediaDTO media) {
        boolean hasPhotos = !media.getPhotos().isEmpty();
        photos.setVisible(hasPhotos);
        if(hasPhotos) {
            ImageGalleryData data = mapPhotoData(media.getPhotos());
            photos.setData(data);
        }
        
        boolean hasVideos = !media.getVideos().isEmpty();
        videos.setVisible(hasVideos);
        if(hasPhotos) {
            VideoGalleryData data = mapVideoData(media.getVideos());
            videos.setData(data);
        }
        
        if(!hasPhotos && !hasVideos) {
            noContent.getStyle().clearDisplay();
        }
    }

    private ImageGalleryData mapPhotoData(ArrayList<MediaEntryDTO> photos) {
        List<ImageDescriptor> images = new ArrayList<>();
        for(MediaEntryDTO entry : photos) {
            ImageDescriptor descriptor = new ImageDescriptor(entry.getMediaURL());
            descriptor.setTitle(entry.getTitle());
            descriptor.setSubtitle(entry.getSubtitle());
            descriptor.setAuthor(entry.getAuthor());
            descriptor.setCreatedAtDate(entry.getCreatedAtDate());
            descriptor.setWidthInPx(entry.getWidthInPx());
            descriptor.setHeightInPx(entry.getHeightInPx());
            descriptor.setThumbnailURL(entry.getThumbnailURL());
            descriptor.setThumbnailWidthInPx(entry.getThumbnailWidthInPx());
            descriptor.setThumbnailHeightInPx(entry.getThumbnailHeightInPx());
            images.add(descriptor);
        }
        return new ImageGalleryData(StringMessages.INSTANCE.photos(), images);
    }
    
    private VideoGalleryData mapVideoData(ArrayList<MediaEntryDTO> photos) {
        List<VideoDescriptor> images = new ArrayList<>();
        for(MediaEntryDTO entry : photos) {
            VideoDescriptor descriptor = new VideoDescriptor(entry.getMediaURL());
            descriptor.setTitle(entry.getTitle());
            descriptor.setSubtitle(entry.getSubtitle());
            descriptor.setAuthor(entry.getAuthor());
            descriptor.setCreatedAtDate(entry.getCreatedAtDate());
            descriptor.setWidthInPx(entry.getWidthInPx());
            descriptor.setHeightInPx(entry.getHeightInPx());
            descriptor.setThumbnailURL(entry.getThumbnailURL());
            descriptor.setThumbnailWidthInPx(entry.getThumbnailWidthInPx());
            descriptor.setThumbnailHeightInPx(entry.getThumbnailHeightInPx());
            images.add(descriptor);
        }
        return new VideoGalleryData(StringMessages.INSTANCE.videos(), images);
    }

}
