package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.media.popup.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.shared.controls.dialog.WindowBox;

public class VideoEmbeddedPlayer extends AbstractEmbeddedMediaPlayer implements VideoPlayer, CloseHandler<PopupPanel>, MediaSynchAdapter {

    private final WindowBox dialogBox;
    private MediaSynchControl mediaSynchControl;

    private final long raceStartTimeMillis;
    private final MediaTrack backupVideoTrack;

    private final MediaServiceAsync mediaService;
    private final Timer raceTimer;
    private final ErrorReporter errorReporter;
    private final PopupCloseListener popupCloseListener;
    private final PopoutListener popoutListener;

    public VideoEmbeddedPlayer(final MediaTrack videoTrack, long raceStartTimeMillis, boolean showSynchControls, Timer raceTimer, MediaServiceAsync mediaService, ErrorReporter errorReporter, PopupCloseListener popCloseListener, PopoutListener popoutListener) {
        super(videoTrack);
        this.raceTimer = raceTimer;
        this.mediaService = mediaService;
        this.errorReporter = errorReporter;
        this.popoutListener = popoutListener;
        backupVideoTrack = new MediaTrack(videoTrack.dbId, videoTrack.title, videoTrack.url, videoTrack.startTime, videoTrack.durationInMillis, videoTrack.mimeType);
        
        
        this.raceStartTimeMillis = raceStartTimeMillis;
        this.popupCloseListener = popCloseListener;

        FlowPanel rootPanel = new FlowPanel();
        rootPanel.addStyleName("FUCK");
        if (mediaControl != null) {
            
            rootPanel.add(mediaControl);
            
            if (showSynchControls) {                
                mediaSynchControl = new MediaSynchControl(this);
                mediaSynchControl.widget().addStyleName("media-synch-control");

                rootPanel.add(mediaSynchControl.widget());
            }
            
        }
        this.dialogBox = new WindowBox(videoTrack.title, videoTrack.toString(), rootPanel, new WindowBox.PopoutHandler() {
            
            @Override
            public void popout() {
                VideoEmbeddedPlayer.this.popoutListener.popoutVideo(videoTrack);
                
            }
        });        
        dialogBox.addCloseHandler(this);
        
        show();
    }
    
    @Override
    protected MediaBase createMediaControl() {
        return Video.createIfSupported();
    }

    private void show() {
        dialogBox.show();
    }
    
    private void hide() {
        dialogBox.hide();
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
        pauseMedia();
        this.popupCloseListener.popupClosed();
    }

    @Override
    public void destroy() {
        super.destroy();
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
        mediaControl.setControls(isVisible);
    }

    @Override
    public void save() {
        
        if (backupVideoTrack.startTime != getMediaTrack().startTime) {
            mediaService.updateStartTime(getMediaTrack(), new AsyncCallback<Void>() {

                @Override
                public void onSuccess(Void result) {
                    // nothing to do
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.toString());
                }
            });
        }
    }

    @Override
    public void discard() {
// For now, only start time can be changed.        
//        getMediaTrack().title = backupVideoTrack.title;
//        getMediaTrack().url = backupVideoTrack.url;
//        getMediaTrack().durationInMillis = backupVideoTrack.durationInMillis;
        getMediaTrack().startTime = backupVideoTrack.startTime;
    }

    @Override
    public void pauseRace() {
        raceTimer.pause();
    }

}
