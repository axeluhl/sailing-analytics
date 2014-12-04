package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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

    private static final MainMediaResources.LocalCss STYLES = MainMediaResources.INSTANCE.css();

    @UiField
    HTMLPanel videosPanel;
    @UiField
    DivElement videoLightBoxData;

    @UiField
    ImageCarousel imageCarousel;

    // private Swiper swiper;
    private int videoCounter;

    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }

    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        videoCounter = 0;

        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setFeaturedEvents(List<Pair<StageEventType, EventBaseDTO>> featuredEvents) {
        for (Pair<StageEventType, EventBaseDTO> featuredEventTypeAndEvent : featuredEvents) {
            if (featuredEventTypeAndEvent.getB().getVideoURLs().size() > 0 && videoCounter < MAX_VIDEO_COUNT) {
                addVideoToVideoPanel(featuredEventTypeAndEvent.getB());
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
        }
        List<Holder> photoGalleryUrls = new LinkedList<>();
        for (EventBaseDTO event : recentEvents) {
            for (String url : event.getSailingLovesPhotographyImages()) {
                ImageSize size = event.getImageSize(url);
                photoGalleryUrls.add(new Holder(url, size.getHeight(), size.getWidth()));
            }
            if (!event.getVideoURLs().isEmpty() && videoCounter < MAX_VIDEO_COUNT) {
                addVideoToVideoPanel(event);
            }
        }

        // shuffle the image url list (Remark: Collections.shuffle() is not implemented in GWT)
        final int gallerySize = photoGalleryUrls.size();
        Random random = new Random(gallerySize);
        for (int i = 0; i < gallerySize; i++) {
            Collections.swap(photoGalleryUrls, i, random.nextInt(gallerySize));
        }

        for (Holder holder : photoGalleryUrls) {

            imageCarousel.addImage(holder.url, holder.height, holder.width);
        }
        imageCarousel.init();
    }

    private void addVideoToVideoPanel(EventBaseDTO event) {
        String youtubeUrl = getRandomVideoURL(event);
        String eventName = event.getName();
        String youtubeId = YoutubeApi.getIdByUrl(youtubeUrl);
        if (youtubeId != null && !youtubeId.trim().isEmpty()) {
            MainMediaVideo video = new MainMediaVideo(eventName, youtubeId);
            videosPanel.add(video);
            videoCounter++;
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
