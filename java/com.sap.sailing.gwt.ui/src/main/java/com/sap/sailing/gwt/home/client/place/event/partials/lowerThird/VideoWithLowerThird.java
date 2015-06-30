package com.sap.sailing.gwt.home.client.place.event.partials.lowerThird;

import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.ui.client.media.PlayEvent;
import com.sap.sailing.gwt.ui.client.media.VideoJSPlayer;
import com.sap.sailing.gwt.ui.shared.general.LabelType;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoWithLowerThird extends Composite {
    private final VideoWithLowerThirdResources.LocalCss style = VideoWithLowerThirdResources.INSTANCE.css();
    private final VideoJSPlayer videoJSPlayer;
    private final LowerThird lowerThird;
    
    public VideoWithLowerThird() {
        this(true, false);
    }
    public VideoWithLowerThird(boolean fullHeightWidth, boolean autoplay) {
        style.ensureInjected();
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(style.videoWithLowerThird());
        videoJSPlayer = new VideoJSPlayer();
        panel.add(videoJSPlayer);
        lowerThird = new LowerThird();
        lowerThird.setVisible(false);
        panel.add(lowerThird);
        initWidget(panel);
        
        videoJSPlayer.addPlayHandler(new PlayEvent.Handler() {
            @Override
            public void onStart(PlayEvent event) {
                lowerThird.setVisible(false);
            }
        });
    }
    
    public void setVideo(VideoDTO video) {
        videoJSPlayer.setVideo(video.getMimeType(), video.getSourceRef());
        if(video.getTitle() != null && !video.getTitle().isEmpty()) {
            lowerThird.setVisible(true);
            lowerThird.setData(video.getTitle(), video.getSubtitle(), LabelType.NONE);
        } else {
            lowerThird.setVisible(false);
        }
    }
    
    public VideoElement getVideoElement() {
        return videoJSPlayer.getVideoElement();
    }
    public boolean isFullscreen() {
        return videoJSPlayer.isFullscreen();
    }
    public boolean paused() {
        return videoJSPlayer.paused();
    }
}
