package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer;

public class VideoJSSyncPlayer extends AbstractMediaPlayer implements VideoSynchPlayer {
    private VideoJSPlayer videoJsDelegate;
    
    private EditFlag editFlag;
    private final TimePoint raceStartTime;
    private final Timer raceTimer;

    public VideoJSSyncPlayer(MediaTrack mediaTrack, TimePoint raceStartTime, Timer raceTimer) {
        super(mediaTrack);
        GWT.debugger();
        videoJsDelegate = new VideoJSPlayer(true, true);
        videoJsDelegate.videoElement.getStyle().setPosition(Position.ABSOLUTE);
        this.raceStartTime = raceStartTime;
        this.raceTimer = raceTimer;
        videoJsDelegate.setVideo(mediaTrack.mimeType, mediaTrack.url, false);
    }

    @Override
    public boolean isMediaPaused() {
        return videoJsDelegate.paused();
    }

    @Override
    public void pauseMedia() {
        if (!isEditing()) {
            videoJsDelegate.pause();
        }
    }

    @Override
    public void playMedia() {
        if (!isEditing()) {
            videoJsDelegate.play();
        }
    }

    @Override
    public double getDuration() {
        return videoJsDelegate.getDuration();
    }

    @Override
    public double getCurrentMediaTime() {
        return videoJsDelegate.getCurrentTime();
    }
    
    @Override
    public long getCurrentMediaTimeMillis() {
        return (long) (getCurrentMediaTime()*1000);
    }

    public void setCurrentMediaTime(double mediaTime) {
        videoJsDelegate.setCurrentTime((int) mediaTime);
    }

    @Override
    public void setPlaybackSpeed(double newPlaySpeedFactor) {
        Window.alert("Playback speed changed! " + newPlaySpeedFactor);
    }

    @Override
    public void setMuted(boolean isToBeMuted) {
        Window.alert("setMuted " + isToBeMuted);
    }

    @Override
    public void shutDown() {
        videoJsDelegate.removeFromParent();
    }

    @Override
    public Widget getWidget() {
        return videoJsDelegate;
    }

    @Override
    public long getOffset() {
        return getMediaTrack().startTime.asMillis() - raceStartTime.asMillis();
    }

    @Override
    public void changeOffsetBy(long delta) {
        getMediaTrack().startTime = getMediaTrack().startTime.plus(delta);
        forceAlign();
    }

    @Override
    public void setControlsVisible(boolean isVisible) {
        Window.alert("Controlls visible");
    }

    @Override
    public void pauseRace() {
        videoJsDelegate.pause();
    }

    @Override
    public void updateOffset() {
        getMediaTrack().startTime = new MillisecondsTimePoint(raceTimer.getTime().getTime() - getCurrentMediaTimeMillis());

    }

    @Override
    protected void alignTime() {
        if (!isEditing()) {
            super.alignTime();
        } 
    }
    
    @Override
    public void setEditFlag(EditFlag editFlag) {
        this.editFlag = editFlag;
    }
    
    private boolean isEditing() {
        return editFlag != null && editFlag.isEditing();
    }

    @Override
    public int getDefaultWidth() {
        return -1;
    }

    @Override
    public int getDefaultHeight() {
        return -1;
    }
}
