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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.mainmedia.MainMediaVideo;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.YoutubeApi;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaEntryDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageDescriptor;
import com.sap.sse.gwt.theme.client.component.imagegallery.ImageGalleryData;
import com.sap.sse.gwt.theme.client.component.videogallery.VideoDescriptor;
import com.sap.sse.gwt.theme.client.component.videogallery.VideoGalleryData;

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
    @UiField DivElement videoWrapper;
    @UiField FlowPanel videosPanel;

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
        imageCarousel.setVisible(hasPhotos);
        if(hasPhotos) {
            Random random = new Random();
            List<MediaEntryDTO> shuffledPhotoGallery = new ArrayList<>(media.getPhotos());
            final int gallerySize = media.getPhotos().size();
            for (int i = 0; i < gallerySize; i++) {
                Collections.swap(shuffledPhotoGallery, i, random.nextInt(gallerySize));
            }
            for (MediaEntryDTO holder : shuffledPhotoGallery) {
                imageCarousel.addImage(holder.getMediaURL(), holder.getHeightInPx(), holder.getWidthInPx());
            }
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
            final int numberOfCandidatesAvailable = media.getVideos().size();
            if (numberOfCandidatesAvailable <= (MAX_VIDEO_COUNT - addedVideoUrls.size())) {
                // add all we have, no randomize
                for (MediaEntryDTO videoCandidateInfo : media.getVideos()) {
                    addVideoToVideoPanel(videoCandidateInfo.getMediaURL(), videoCandidateInfo.getTitle());
                }
            } else {
                // fill up the list randomly from videoCandidates
                final Random videosRandomizer = new Random(numberOfCandidatesAvailable);
                randomlyPick: for (int i = 0; i < numberOfCandidatesAvailable; i++) {
                    int nextVideoindex = videosRandomizer.nextInt(numberOfCandidatesAvailable);
                    final MediaEntryDTO videoCandidateInfo = media.getVideos().get(nextVideoindex);
                    final String youtubeUrl = videoCandidateInfo.getMediaURL();
                    addVideoToVideoPanel(youtubeUrl, videoCandidateInfo.getTitle());
                    if (addedVideoUrls.size() == MAX_VIDEO_COUNT) {
                        break randomlyPick;
                    }
                }
            }
        }
        
        
        if(!hasPhotos && !hasVideos) {
            noContent.getStyle().clearDisplay();
        }
    }
    
 // TODO remove -> temporary solution to get contents on the page
    private static final int MAX_VIDEO_COUNT = 3;
    private final HashSet<String> addedVideoUrls = new HashSet<String>(MAX_VIDEO_COUNT);
    private SimplePanel contentPanel;
    private void addVideoToVideoPanel(String youtubeUrl, String title) {
        if (addedVideoUrls.contains(youtubeUrl)) {
            return;
        }
        String youtubeId = YoutubeApi.getIdByUrl(youtubeUrl);
        if (youtubeId != null && !youtubeId.trim().isEmpty()) {
            MainMediaVideo video = new MainMediaVideo(title, youtubeId);
            videosPanel.add(video);
            addedVideoUrls.add(youtubeUrl);
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
