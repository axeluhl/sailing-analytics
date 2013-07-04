package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.media.popup.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.shared.controls.dialog.WindowBox;

public class YoutubeEmbeddedPlayer extends AbstractMediaPlayer implements VideoPlayer, CloseHandler<PopupPanel>, MediaSynchAdapter {

    private final WindowBox dialogBox;
    private MediaSynchControl mediaSynchControl;

    private final long raceStartTimeMillis;

    private final Timer raceTimer;
    private final PopupCloseListener popupCloseListener;
    private final YoutubeVideoControl videoControl;
    private final PopoutListener popoutListener;

    public YoutubeEmbeddedPlayer(final MediaTrack videoTrack, long raceStartTimeMillis, boolean showSynchControls, Timer raceTimer, MediaServiceAsync mediaService, ErrorReporter errorReporter, PopupCloseListener popupCloseListener, PopoutListener popoutListener) {
        super(videoTrack);
        this.raceTimer = raceTimer;
        this.raceStartTimeMillis = raceStartTimeMillis;
        this.popupCloseListener = popupCloseListener;
        this.popoutListener = popoutListener;

        FlowPanel rootPanel = new FlowPanel();
        rootPanel.addStyleName("video-root-panel");

        videoControl = new YoutubeVideoControl(videoTrack.url, showSynchControls); 
        rootPanel.add(videoControl.widget());
        
        if (showSynchControls) {
            mediaSynchControl = new MediaSynchControl(this, mediaService, errorReporter);
            mediaSynchControl.widget().addStyleName("media-synch-control");
            rootPanel.add(mediaSynchControl.widget());
        }

        this.dialogBox = new WindowBox(videoTrack.title, videoTrack.toString(), rootPanel, new WindowBox.PopoutHandler() {
            
            @Override
            public void popout() {
                YoutubeEmbeddedPlayer.this.popoutListener.popoutVideo(videoTrack);
                
            }
        });
        dialogBox.addCloseHandler(this);
        
        show();
    }
    
    private void show() {
        dialogBox.show();
    }
    
    private void hide() {
        dialogBox.hide();
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
//        pauseMedia();
        this.popupCloseListener.popupClosed();
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public long getOffset() {
        return getMediaTrack().startTime.getTime() - raceStartTimeMillis;
    }

    @Override
    public void changeOffsetBy(long delta) {
        getMediaTrack().startTime = new Date(getMediaTrack().startTime.getTime() + delta);
        forceAlign();
    }

    @Override
    public void updateOffset() {
        getMediaTrack().startTime = new Date(raceTimer.getTime().getTime() - getCurrentMediaTimeMillis());
    }

    @Override
    public void setControlsVisible(boolean isVisible) {
        videoControl.setControlsVisible(isVisible);
    }

    @Override
    public void pauseRace() {
        raceTimer.pause();
    }

    @Override
    public boolean isMediaPaused() {
        return videoControl.isPaused();
    }

    @Override
    public void pauseMedia() {
        if (!isEdited()) {
            videoControl.pause();
        }
    }

    @Override
    public void playMedia() {
        if (!isEdited()) {
            videoControl.play();
        }
    }

    @Override
    public double getDuration() {
        return videoControl.getDuration();
    }

    @Override
    public double getCurrentMediaTime() {
        return videoControl.getCurrentTime();
    }

    @Override
    public void setCurrentMediaTime(double mediaTime) {
        videoControl.setCurrentTime(mediaTime);
    }

    @Override
    public void setPlaybackSpeed(double newPlaySpeedFactor) {
        videoControl.setPlaybackSpeed(newPlaySpeedFactor);
    }

    @Override
    public void setMuted(boolean isToBeMuted) {
        videoControl.setMuted(isToBeMuted);
    }
    
    @Override
    protected void alignTime() {
        if (!isEdited()) {
            super.alignTime();
        } 
    }

    private boolean isEdited() {
        return mediaSynchControl != null && mediaSynchControl.isEditing();
    }

}
