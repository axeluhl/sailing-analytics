package com.sap.sailing.gwt.home.shared.partials.videoplayer;

import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.ui.client.media.PauseEvent;
import com.sap.sailing.gwt.ui.client.media.PlayEvent;
import com.sap.sailing.gwt.ui.client.media.VideoJSPlayer;
import com.sap.sse.gwt.client.media.VideoDTO;

/**
 * Video player with custom play button.
 */
public class VideoPlayer extends Composite {
    private final VideoPlayerResources.LocalCss style = VideoPlayerResources.INSTANCE.css();
    private VideoJSPlayer videoJSPlayer;
    protected final FlowPanel panel;
    private final PlayButton playButton = new PlayButton();
    
    private boolean initialized = false;
    
    public VideoPlayer() {
        this(true, false);
    }
    public VideoPlayer(boolean fullHeightWidth, boolean autoplay) {
        style.ensureInjected();
        panel = new FlowPanel();
        panel.addStyleName(style.videoPlayer());
        videoJSPlayer = new VideoJSPlayer(fullHeightWidth, autoplay);
        videoJSPlayer.addPlayHandler(new PlayEvent.Handler() {
            @Override
            public void onStart(PlayEvent event) {
                onPlay();
            }
        });
        videoJSPlayer.addPauseHandler(new PauseEvent.Handler() {
            @Override
            public void onPause(PauseEvent event) {
                onPaused();
            }
        });
        panel.add(videoJSPlayer);
        playButton.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                videoJSPlayer.play();
                playButton.setVisible(false);
            }
        }, ClickEvent.getType());
        panel.add(playButton);
        initWidget(panel);
    }
    
    protected void initialize() {
    }
    
    protected void onPlay() {
        playButton.setVisible(false);
    }
    
    protected void onPaused() {
        playButton.setVisible(true);
    }
    

    public void setVideo(VideoDTO video) {
        if(!initialized) {
            initialize();
        }
        videoJSPlayer.setVideo(video.getMimeType(), video.getSourceRef());
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

    public void play() {
        videoJSPlayer.play();
    }
}
