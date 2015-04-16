package com.sap.sailing.gwt.home.client.shared.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.mainmedia.MainMediaVideo;
import com.sap.sailing.gwt.ui.shared.media.ImageMetadataDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.media.VideoMetadataDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;

public class MediaPage extends Composite {

    private static MediaPageUiBinder uiBinder = GWT.create(MediaPageUiBinder.class);

    interface MediaPageUiBinder extends UiBinder<Widget, MediaPage> {
    }
    
    @UiField HeadingElement noContent;
    // TODO use this when ImageGallery and VideoGallery is implemented correctly
//    @UiField ImageGallery photos;
//    @UiField VideoGallery videos;
    // TODO remove -> temporary solution to get contents on the page
    @UiField ImageCarousel imageCarousel;
    @UiField DivElement photoWrapper;
    @UiField DivElement videoWrapper;
    @UiField DivElement videosPanel;

    public MediaPage() {
        MediaPageResources.INSTANCE.css().ensureInjected();
        contentPanel = new SimplePanel();
        initWidget(contentPanel);
    }
    
    public void setMedia(MediaDTO media) {
        contentPanel.setWidget(uiBinder.createAndBindUi(this));
        
        boolean hasPhotos = !media.getPhotos().isEmpty();
     // TODO use this when ImageGallery is implemented correctly
//        photos.setVisible(hasPhotos);
//        if(hasPhotos) {
//            ImageGalleryData data = mapPhotoData(media.getPhotos());
//            photos.setData(data);
//        }
     // TODO remove -> temporary solution to get contents on the page
        if(hasPhotos) {
            photoWrapper.getStyle().clearDisplay();
            Random random = new Random();
            List<ImageMetadataDTO> shuffledPhotoGallery = new ArrayList<>(media.getPhotos());
            final int gallerySize = media.getPhotos().size();
            for (int i = 0; i < gallerySize; i++) {
                Collections.swap(shuffledPhotoGallery, i, random.nextInt(gallerySize));
            }
            int count = 0;
            do {
                for (ImageMetadataDTO holder : shuffledPhotoGallery) {
                    imageCarousel.addImage(holder.getImageURL(), holder.getHeightInPx(), holder.getWidthInPx());
                    count++;
                }
            } while (count < 3);
        }
        
        boolean hasVideos = !media.getVideos().isEmpty();
        // TODO use this when VideoGallery is implemented correctly
//        videos.setVisible(hasVideos);
//        if(hasPhotos) {
//            VideoGalleryData data = mapVideoData(media.getVideos());
//            videos.setData(data);
//        }
     // TODO remove -> temporary solution to get contents on the page
        if(hasVideos) {
            videoWrapper.getStyle().clearDisplay();
            if(hasPhotos) {
                videoWrapper.addClassName(MediaPageResources.INSTANCE.css().dark());
            }
            for (VideoMetadataDTO videoCandidateInfo : media.getVideos()) {
                addVideoToVideoPanel(videoCandidateInfo.getRef(), videoCandidateInfo.getTitle());
            }
        }
        
        
        if(!hasPhotos && !hasVideos) {
            noContent.getStyle().clearDisplay();
        }
    }
    
 // TODO remove -> temporary solution to get contents on the page
    private static final int MAX_VIDEO_COUNT = 3;
    private final HashSet<String> addedVideoIds = new HashSet<String>(MAX_VIDEO_COUNT);
    private SimplePanel contentPanel;
    private void addVideoToVideoPanel(String youtubeId, String title) {
        if (addedVideoIds.contains(youtubeId)) {
            return;
        }
        if (youtubeId != null && !youtubeId.trim().isEmpty()) {
            MainMediaVideo video = new MainMediaVideo(title, youtubeId);
            videosPanel.appendChild(video.getElement());
            addedVideoIds.add(youtubeId);
        }
    }

    // private ImageGalleryData mapPhotoData(ArrayList<MediaEntryDTO> photos) {
    // List<ImageDescriptor> images = new ArrayList<>();
    // for(MediaEntryDTO entry : photos) {
    // ImageDescriptor descriptor = new ImageDescriptor(entry.getMediaURL());
    // descriptor.setTitle(entry.getTitle());
    // descriptor.setSubtitle(entry.getSubtitle());
    // descriptor.setAuthor(entry.getAuthor());
    // descriptor.setCreatedAtDate(entry.getCreatedAtDate());
    // descriptor.setWidthInPx(entry.getWidthInPx());
    // descriptor.setHeightInPx(entry.getHeightInPx());
    // descriptor.setThumbnailURL(entry.getThumbnailURL());
    // descriptor.setThumbnailWidthInPx(entry.getThumbnailWidthInPx());
    // descriptor.setThumbnailHeightInPx(entry.getThumbnailHeightInPx());
    // images.add(descriptor);
    // }
    // return new ImageGalleryData(StringMessages.INSTANCE.photos(), images);
    // }
    //
    // private VideoGalleryData mapVideoData(ArrayList<MediaEntryDTO> photos) {
    // List<VideoDescriptor> images = new ArrayList<>();
    // for(MediaEntryDTO entry : photos) {
    // VideoDescriptor descriptor = new VideoDescriptor(entry.getMediaURL());
    // descriptor.setTitle(entry.getTitle());
    // descriptor.setSubtitle(entry.getSubtitle());
    // descriptor.setAuthor(entry.getAuthor());
    // descriptor.setCreatedAtDate(entry.getCreatedAtDate());
    // descriptor.setWidthInPx(entry.getWidthInPx());
    // descriptor.setHeightInPx(entry.getHeightInPx());
    // descriptor.setThumbnailURL(entry.getThumbnailURL());
    // descriptor.setThumbnailWidthInPx(entry.getThumbnailWidthInPx());
    // descriptor.setThumbnailHeightInPx(entry.getThumbnailHeightInPx());
    // images.add(descriptor);
    // }
    // return new VideoGalleryData(StringMessages.INSTANCE.videos(), images);
    // }

}
