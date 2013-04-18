package com.sap.sailing.gwt.ui.video;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;

public abstract class AbstractPopupWindow implements EntryPoint {

    @Override
    public void onModuleLoad() {
        initializePlayer();
    }
    
    protected abstract void initializePlayer();

    native void addCallbackMethods() /*-{
		var that = this;
		$wnd.videoPlayer = {
			play : function() {
				that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::play()();
			},
			pause : function() {
				that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::pause()();
			},
			setTime : function(time) {
				that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::setTime(D)(time);
			},
			setMuted : function(muted) {
				that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::setMuted(Z)(muted);
			},
			isPaused : function() {
				return that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::isPaused()();
			},
			getDuration : function() {
				return that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::getDuration()();
			},
			getTime : function() {
				return that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::getTime()();
			},
			setPlaybackSpeed : function(newPlaySpeedFactor) {
				that.@com.sap.sailing.gwt.ui.video.AbstractPopupWindow::setPlaybackSpeed(D)(newPlaySpeedFactor);
			}
		};
    }-*/;

    public abstract void play();

    public abstract void pause();

    public abstract void setTime(double time);

    public abstract void setMuted(boolean muted);

    public abstract boolean isPaused();

    public abstract double getDuration();

    public abstract double getTime();

    public abstract void setPlaybackSpeed(double newPlaySpeedFactor);

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

    protected void adjustWindowSize() {
        int windowWidth = Window.getClientWidth();
        int videoWidth = getVideoWidth();
        int widthDelta = videoWidth - windowWidth;

        int windowHeight = Window.getClientHeight();
        int videoHeight = getVideoHeight();
        int heightDelta = videoHeight - windowHeight;

        Window.resizeBy(widthDelta, heightDelta);
    }

    protected abstract boolean hasVideoSizes();
    
    protected abstract int getVideoWidth();
    
    protected abstract int getVideoHeight();
    
    protected abstract void setVideoSize(int width, int height);
    
}
