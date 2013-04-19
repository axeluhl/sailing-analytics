package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.media.popup.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.shared.controls.dialog.WindowBox;
import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack;

public class YoutubeEmbeddedPlayer extends AbstractMediaPlayer implements VideoPlayer, CloseHandler<PopupPanel>, MediaSynchAdapter {

    private final WindowBox dialogBox;
    private MediaSynchControl mediaSynchControl;

    private final long raceStartTimeMillis;
    private final MediaTrack backupVideoTrack;

    private final MediaServiceAsync mediaService;
    private final Timer raceTimer;
    private final ErrorReporter errorReporter;
    private final PopupCloseListener popupCloseListener;
    private final YoutubeVideoControl videoControl;

    public YoutubeEmbeddedPlayer(MediaTrack videoTrack, long raceStartTimeMillis, boolean showSynchControls, Timer raceTimer, MediaServiceAsync mediaService, ErrorReporter errorReporter, PopupCloseListener popupCloseListener) {
        super(videoTrack);
        this.raceTimer = raceTimer;
        this.raceStartTimeMillis = raceStartTimeMillis;
        this.popupCloseListener = popupCloseListener;
        this.mediaService = mediaService;
        this.errorReporter = errorReporter;
        backupVideoTrack = new MediaTrack(null, videoTrack.title, videoTrack.url, videoTrack.startTime, videoTrack.durationInMillis, videoTrack.mimeType);

        this.dialogBox = new WindowBox(false, false, true, true);
        dialogBox.setText(videoTrack.title);        
        dialogBox.setTitle(videoTrack.toString());        

        VerticalPanel rootPanel = new VerticalPanel();
        if (showSynchControls) {
            mediaSynchControl = new MediaSynchControl(this);
            rootPanel.add(mediaSynchControl.widget());
        }

        videoControl = new YoutubeVideoControl(videoTrack.url); 
        rootPanel.add(videoControl.widget());
        dialogBox.setWidget(rootPanel);
        
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
    public void destroy() {
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
    public void save() {
        mediaService.saveChanges(this.getMediaTrack(), new AsyncCallback<Void>() {
            
            @Override
            public void onSuccess(Void result) {
                //nothing to do
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.toString());
            }
        });
    }

    @Override
    public void discard() {
        getMediaTrack().title = backupVideoTrack.title;
        getMediaTrack().url = backupVideoTrack.url;
        getMediaTrack().startTime = backupVideoTrack.startTime;
        getMediaTrack().durationInMillis = backupVideoTrack.durationInMillis;
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
        videoControl.pause();
    }

    @Override
    public void playMedia() {
        videoControl.play();
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

}
