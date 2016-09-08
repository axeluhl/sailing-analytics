package com.sap.sailing.gwt.home.shared.partials.videoplayer;

import com.sap.sailing.gwt.home.communication.event.LabelType;
import com.sap.sailing.gwt.home.shared.partials.lowerthird.LowerThird;
import com.sap.sse.gwt.client.media.VideoDTO;

/**
 * Video player with a lower third containing title, subtitle and an optional {@link LabelType}.
 */
public class VideoWithLowerThird extends VideoPlayer {
    private final LowerThird lowerThird = new LowerThird();
    
    public VideoWithLowerThird() {
        this(true, false);
    }
    public VideoWithLowerThird(boolean fullHeightWidth, boolean autoplay) {
        super(fullHeightWidth, autoplay);
    }
    
    @Override
    protected void initialize() {
        lowerThird.setVisible(false);
        panel.add(lowerThird);
    }
    
    @Override
    protected void onPlay() {
        super.onPlay();
        lowerThird.setVisible(false);
    }
    
    public void setVideo(VideoDTO video) {
        super.setVideo(video);
        if(video.getTitle() != null && !video.getTitle().isEmpty()) {
            lowerThird.setVisible(paused());
            lowerThird.setData(video.getTitle(), video.getSubtitle(), LabelType.NONE);
        } else {
            lowerThird.setVisible(false);
        }
    }
}
