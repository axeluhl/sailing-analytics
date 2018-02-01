package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.video;

import java.util.function.Consumer;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoPlace extends Place {
    private VideoDTO videoToPlay;
    private Consumer<Integer> durationConsumer;

    private VideoPlace() {
    }

    public VideoPlace(VideoDTO videoToPlay, Consumer<Integer> durationConsumer) {
        this.videoToPlay = videoToPlay;
        this.durationConsumer = durationConsumer;
    }

    public VideoDTO getVideoToPlay() {
        return videoToPlay;
    }


    public static class Tokenizer implements PlaceTokenizer<VideoPlace> {
        @Override
        public String getToken(VideoPlace place) {
            return "";
        }

        @Override
        public VideoPlace getPlace(String token) {
            return new VideoPlace();
        }
    }


    public void publishDuration(int durationInSeconds) {
        if (durationConsumer != null) {
            durationConsumer.accept(durationInSeconds);
        }
    }
}
