package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.media.popup.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sse.gwt.client.dialog.WindowBox;

public class YoutubeEmbeddedPlayer extends AbstractMediaPlayer implements VideoPlayer, CloseHandler<PopupPanel>, MediaSynchAdapter {

    private static int videoCounter;
    
    private final WindowBox dialogBox;
    private MediaSynchControl mediaSynchControl;

    private final long raceStartTimeMillis;

    private final Timer raceTimer;
    private final PopupCloseListener popupCloseListener;
    private final YoutubeVideoControl videoControl;
    private final PopoutListener popoutListener;

    /**
     * Required to indicate whether this control has been requested to close.
     * Then, it must not call any player functions anymore
     * not to cause null-access error due to missing DOM elements.
     */
    private boolean closing = false;

    public YoutubeEmbeddedPlayer(final MediaTrack videoTrack, long raceStartTimeMillis, boolean showSynchControls, Timer raceTimer, MediaServiceAsync mediaService, ErrorReporter errorReporter, PopupCloseListener popupCloseListener, PopoutListener popoutListener) {
        super(videoTrack);
        this.raceTimer = raceTimer;
        this.raceStartTimeMillis = raceStartTimeMillis;
        this.popupCloseListener = popupCloseListener;
        this.popoutListener = popoutListener;

        FlowPanel rootPanel = new FlowPanel();
        rootPanel.addStyleName("video-root-panel");

        Panel videoContainer = new SimplePanel();

        String videoContainerId = "videoContainer-" + videoTrack.url + ++videoCounter;
        videoContainer.getElement().setId(videoContainerId);
        videoContainer.getElement().setInnerText("When the Youtube video doesn't show up, click the popout button at the upper right corner to open the video in a dedicated browser window.");

        rootPanel.add(videoContainer.asWidget());
        
        this.dialogBox = new WindowBox(videoTrack.title, videoTrack.toString(), rootPanel, new WindowBox.PopoutHandler() {
            
            @Override
            public void popout() {
                YoutubeEmbeddedPlayer.this.popoutListener.popoutVideo(videoTrack);
                
            }
        });
        dialogBox.addCloseHandler(this);
        
        //first show dialog to make video container visible in DOM
        show();
        
        //then use Youtube API to render load video into video container
        videoControl = new YoutubeVideoControl(videoTrack.url, videoContainerId, showSynchControls); 

        //then show media synch controls which refer to the video control
        if (showSynchControls) {
            mediaSynchControl = new MediaSynchControl(this, mediaService, errorReporter);
            mediaSynchControl.widget().addStyleName("media-synch-control");
            rootPanel.add(mediaSynchControl.widget());
        }

    }
    
    private void show() {
        dialogBox.show();
    }
    
    private void hide() {
        dialogBox.hide();
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
        this.closing  = true;
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
        if (!isEdited() && !this.closing) {
            videoControl.pause();
        }
    }

    @Override
    public void playMedia() {
        if (!isEdited() && !this.closing) {
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
