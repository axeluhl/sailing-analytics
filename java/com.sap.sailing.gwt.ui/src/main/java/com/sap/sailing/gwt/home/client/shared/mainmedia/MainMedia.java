package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.stage.StageEventType;
import com.sap.sailing.gwt.ui.common.client.YoutubeApi;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;

public class MainMedia extends Composite {

    private static final int MAX_VIDEO_COUNT = 3;

    @UiField
    HTMLPanel videosPanel;

    @UiField
    DivElement videoLightBoxData;

    @UiField
    ImageCarousel imageCarousel;

    private final HashSet<String> addedVideoUrls = new HashSet<String>(MAX_VIDEO_COUNT);

    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }

    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setFeaturedEvents(List<Pair<StageEventType, EventBaseDTO>> featuredEvents) {
        for (Pair<StageEventType, EventBaseDTO> featuredEventTypeAndEvent : featuredEvents) {
            if (featuredEventTypeAndEvent.getB().getVideoURLs().size() > 0 && addedVideoUrls.size() < MAX_VIDEO_COUNT) {
                String youTubeRandomUrl = getRandomVideoURL(featuredEventTypeAndEvent.getB());
                addVideoToVideoPanel(youTubeRandomUrl, featuredEventTypeAndEvent.getB());
            }
        }
    }

    public void setRecentEvents(List<EventBaseDTO> recentEvents) {
        class Holder {
            public Holder(String url, int height, int width) {
                this.url = url;
                this.height = height;
                this.width = width;
            }
            String url;
            int height;
            int width;
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + height;
                result = prime * result + ((url == null) ? 0 : url.hashCode());
                result = prime * result + width;
                return result;
            }
            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                Holder other = (Holder) obj;
                if (height != other.height)
                    return false;
                if (url == null) {
                    if (other.url != null)
                        return false;
                } else if (!url.equals(other.url))
                    return false;
                if (width != other.width)
                    return false;
                return true;
            }
        }

        final Set<Holder> photoGalleryUrls = new HashSet<>(); // using a HashSet here leads to a reasonable amount of shuffling
        final List<Pair<String, EventBaseDTO>> videoCandidates = new LinkedList<>();

        for (EventBaseDTO event : recentEvents) {
            for (String url : event.getSailingLovesPhotographyImages()) {
                ImageSize size = event.getImageSize(url);
                photoGalleryUrls.add(new Holder(url, size.getHeight(), size.getWidth()));
            }
            for (String videoUrl : event.getVideoURLs()) {
                videoCandidates.add(new Pair<String, EventBaseDTO>(videoUrl, event));
            }
        }

        final int numberOfCandidatesAvailable = videoCandidates.size();
        if (numberOfCandidatesAvailable <= (MAX_VIDEO_COUNT - addedVideoUrls.size())) {
            // add all we have, no randomize
            for (Pair<String, EventBaseDTO> videoCandidateInfo : videoCandidates) {
                addVideoToVideoPanel(videoCandidateInfo.getA(), videoCandidateInfo.getB());
            }
        } else {
            // fill up the list randomly from videoCandidates
            final Random videosRandomizer = new Random(numberOfCandidatesAvailable);
            randomlyPick: for (int i = 0; i < numberOfCandidatesAvailable; i++) {
                int nextVideoindex = videosRandomizer.nextInt(numberOfCandidatesAvailable);
                final Pair<String, EventBaseDTO> videoCandidateInfo = videoCandidates.get(nextVideoindex);
                final String youtubeUrl = videoCandidateInfo.getA();
                addVideoToVideoPanel(youtubeUrl, videoCandidateInfo.getB());
                if (addedVideoUrls.size() == MAX_VIDEO_COUNT) {
                    break randomlyPick;
                }
            }
        }
        Random random = new Random();
        List<Holder> shuffledPhotoGallery = new ArrayList<>(photoGalleryUrls);
        final int gallerySize = photoGalleryUrls.size();
        for (int i = 0; i < gallerySize; i++) {
            Collections.swap(shuffledPhotoGallery, i, random.nextInt(gallerySize));
        }
        for (Holder holder : shuffledPhotoGallery) {
            imageCarousel.addImage(holder.url, holder.height, holder.width);
        }
    }

    private void addVideoToVideoPanel(String youtubeUrl, EventBaseDTO event) {
        if (addedVideoUrls.contains(youtubeUrl)) {
            return;
        }
        String eventName = event.getName();
        String youtubeId = YoutubeApi.getIdByUrl(youtubeUrl);
        if (youtubeId != null && !youtubeId.trim().isEmpty()) {
            MainMediaVideo video = new MainMediaVideo(eventName, youtubeId);
            videosPanel.add(video);
            addedVideoUrls.add(youtubeUrl);
        }
    }

    private String getRandomVideoURL(EventBaseDTO event) {
        final String result;
        List<String> videoURLs = event.getVideoURLs();
        if (!videoURLs.isEmpty()) {
            result = event.getVideoURLs().get(new Random(videoURLs.size()).nextInt(videoURLs.size()));
        } else {
            result = null;
        }
        return result;
    }



}
