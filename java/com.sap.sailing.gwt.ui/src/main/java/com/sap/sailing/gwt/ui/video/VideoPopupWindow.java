package com.sap.sailing.gwt.ui.video;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class VideoPopupWindow implements EntryPoint, ResizeHandler, ContextMenuHandler {

    private Video video;
    private boolean isDebug;

    @Override
    public void onModuleLoad() {

        Window.addResizeHandler(this);

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
			that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::loadedmetadata()();
		});
                videoElement.addEventListener('canplay', function() { //see http://www.w3schools.com/tags/av_event_canplay.asp
                        var deferredPlayState = $wnd.deferredPlayState
                        if (deferredPlayState && !$wnd.videoPlayer) {
                            that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::initPlayState(DZDZ)(deferredPlayState.deferredMediaTime, deferredPlayState.isDeferredMuted, deferredPlayState.deferredPlaybackSpeed, deferredPlayState.isDeferredPlaying);
                        }
                });
    }-*/;

    native void addCallbackMethods() /*-{
		var that = this;
		$wnd.videoPlayer = {
			play : function() {
				that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::play()();
			},
			pause : function() {
				that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::pause()();
			},
			setTime : function(time) {
				that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::setTime(D)(time);
			},
			setMuted : function(muted) {
				that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::setMuted(Z)(muted);
			},
			isPaused : function() {
				return that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::isPaused()();
			},
			getDuration : function() {
				return that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::getDuration()();
			},
			getTime : function() {
				return that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::getTime()();
			},
			setPlaybackSpeed : function(newPlaySpeedFactor) {
				that.@com.sap.sailing.gwt.ui.video.VideoPopupWindow::setPlaybackSpeed(D)(newPlaySpeedFactor);
			}
		};
    }-*/;

    public void play() {
        video.play();
    }

    public void pause() {
        video.pause();
    }

    public void setTime(double time) {
        video.setCurrentTime(time);
    }

    public void setMuted(boolean muted) {
        video.setMuted(muted);
    }

    public boolean isPaused() {
        return video.isPaused();
    }

    public double getDuration() {
        return video.getDuration();
    }

    public double getTime() {
        return video.getCurrentTime();
    }

    public void setPlaybackSpeed(double newPlaySpeedFactor) {
        video.setPlaybackRate(newPlaySpeedFactor);
    }

    public void loadedmetadata() {
        adjustWindowSize();
    }
    
    public void initPlayState(double deferredMediaTime, boolean isDeferredMuted, double deferredPlaybackSpeed,
            boolean isDeferredPlaying) {
        
        addCallbackMethods();
        
        setTime(deferredMediaTime);
        setMuted(isDeferredMuted);
        setPlaybackSpeed(deferredPlaybackSpeed);
        if (isDeferredPlaying) {
            play();
        } else {
            pause();
        }

    }

    private void adjustWindowSize() {
        int clientWidth = Window.getClientWidth();
        int videoWidth = video.getVideoWidth();
        int widthDelta = videoWidth - clientWidth;

        int clientHeight = Window.getClientHeight();
        int videoHeight = video.getVideoHeight();
        int heightDelta = videoHeight - clientHeight;

        Window.resizeBy(widthDelta, heightDelta);
    }

    @Override
    public void onResize(final ResizeEvent event) {
        if (video != null) {
            new Timer() {
                @Override
                public void run() {
                    int videoWidth = video.getVideoWidth();
                    if (videoWidth > 0) {
                        int clientWidth = Window.getClientWidth();
                        double widthResizeRatio = ((double) clientWidth) / videoWidth;

                        int videoHeight = video.getVideoHeight();
                        if (videoHeight > 0) {
                            int clientHeight = Window.getClientHeight();
                            double heightResizeRatio = ((double) clientHeight) / videoHeight;

                            double resizeRatio = Math.min(widthResizeRatio, heightResizeRatio);

                            if (Math.abs(1.0 - resizeRatio) > 0.001) {

                                int newVideoWidth = (int) Math.round(resizeRatio * videoWidth);
                                int newVideoHeight = (int) Math.round(resizeRatio * videoHeight);
                                video.setPixelSize(newVideoWidth, newVideoHeight);

                                int widthDelta = newVideoWidth - clientWidth;
                                int heightDelta = newVideoHeight - clientHeight;

                                Window.resizeBy(widthDelta, heightDelta);
                            }
                        }
                    }

                }
            }.schedule(100);
        }
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
    
}
