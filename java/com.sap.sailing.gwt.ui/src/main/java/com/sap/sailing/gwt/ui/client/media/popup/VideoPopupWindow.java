package com.sap.sailing.gwt.ui.client.media.popup;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class VideoPopupWindow extends AbstractPopupWindow implements ContextMenuHandler {

    private Video video;
    private boolean isDebug;

    @Override
    protected void initializePlayer() {

        RootLayoutPanel mainPanel = RootLayoutPanel.get();

        String title = Window.Location.getParameter("title");
        Window.setTitle(title);

        String videoUrl = Window.Location.getParameter("url");
        
        isDebug = Window.Location.getParameter("gwt.codesvr") != null;
        
        if (videoUrl != null) {
            video = Video.createIfSupported();
            if (video != null) {

                addNativeEventHandlers(video.getVideoElement());
                video.addDomHandler(this, ContextMenuEvent.getType());

                video.setPreload(MediaElement.PRELOAD_AUTO);
                video.setMuted(true);
                video.setAutoplay(false);
                video.setControls(false);
                video.setLoop(false);
                video.setSrc(videoUrl);
                mainPanel.add(video);

            } else {
                mainPanel.add(new Label("Video not supported"));
            }
        } else {
            mainPanel.add(new Label("Parameter 'url' not assigned."));
        }

    }

    native void addNativeEventHandlers(VideoElement videoElement) /*-{
		var that = this;
		videoElement.addEventListener('loadedmetadata', function() {
			that.@com.sap.sailing.gwt.ui.client.media.popup.VideoPopupWindow::loadedmetadata()();
		});
                videoElement.addEventListener('canplay', function() { //see http://www.w3schools.com/tags/av_event_canplay.asp
                        var deferredPlayState = $wnd.deferredPlayState
                        if (deferredPlayState && !$wnd.videoPlayer) {
                            that.@com.sap.sailing.gwt.ui.client.media.popup.VideoPopupWindow::initPlayState(DZDZ)(deferredPlayState.deferredMediaTime, deferredPlayState.deferredIsMuted, deferredPlayState.deferredPlaybackSpeed, deferredPlayState.deferredIsPlaying);
                        }
                });
    }-*/;

    public void loadedmetadata() {
        adjustWindowSize();
    }
    
    @Override
    public void play() {
        video.play();
    }

    @Override
    public void pause() {
        video.pause();
    }

    @Override
    public void setTime(double time) {
        video.setCurrentTime(Math.min(time, video.getDuration()));
    }

    @Override
    public void setMuted(boolean muted) {
        video.setMuted(muted);
    }

    @Override
    public boolean isPaused() {
        return video.isPaused();
    }

    @Override
    public double getDuration() {
        return video.getDuration();
    }

    @Override
    public double getTime() {
        return video.getCurrentTime();
    }

    @Override
    public void setPlaybackSpeed(double newPlaySpeedFactor) {
        video.setPlaybackRate(newPlaySpeedFactor);
    }

    /**
     * Suppressing context menu.
     */
    @Override
    public void onContextMenu(ContextMenuEvent event) {
        if (!isDebug) {
            event.preventDefault();
            event.stopPropagation();
        }
    }

    @Override
    protected boolean hasVideoSizes() {
        return video != null;
    }
    
    @Override
    protected int getVideoWidth() {
        return video.getVideoWidth();
    }

    @Override
    protected int getVideoHeight() {
        return video.getVideoHeight();
    }

    @Override
    protected void setVideoSize(int width, int height) {
        video.setPixelSize(width, height);
    }

}
