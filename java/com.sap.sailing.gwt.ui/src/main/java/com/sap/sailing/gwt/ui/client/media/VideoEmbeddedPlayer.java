package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.media.popup.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.shared.controls.dialog.WindowBox;
import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack;

public class VideoEmbeddedPlayer extends AbstractEmbeddedMediaPlayer implements VideoPlayer, CloseHandler<PopupPanel>, MediaSynchAdapter {

    private final WindowBox dialogBox;
    private MediaSynchControl mediaSynchControl;

    private final long raceStartTimeMillis;
    private final MediaTrack backupVideoTrack;

    private final MediaServiceAsync mediaService;
    private final Timer raceTimer;
    private final ErrorReporter errorReporter;
    private final PopupCloseListener popupCloseListener;

    public VideoEmbeddedPlayer(MediaTrack videoTrack, long raceStartTimeMillis, boolean showSynchControls, Timer raceTimer, MediaServiceAsync mediaService, ErrorReporter errorReporter, PopupCloseListener popCloseListener) {
        super(videoTrack);
        this.raceTimer = raceTimer;
        this.mediaService = mediaService;
        this.errorReporter = errorReporter;
        backupVideoTrack = new MediaTrack(videoTrack.dbId, videoTrack.title, videoTrack.url, videoTrack.startTime, videoTrack.durationInMillis, videoTrack.mimeType);
        
        
        this.raceStartTimeMillis = raceStartTimeMillis;
        this.popupCloseListener = popCloseListener;

        this.dialogBox = new WindowBox(false, false, true, true);
        dialogBox.setText(videoTrack.title);        
        dialogBox.setTitle(videoTrack.toString());        

        if (mediaControl != null) {
            
            
            // HTML videoFrame = new
            // HTML("<iframe class=\"youtube-player\" type=\"text/html\" width=\"640\" height=\"385\" src=\"http://www.youtube.com/embed/dP15zlyra3c?html5=1\" frameborder=\"0\"></iframe>");
            //
            // SimplePanel videoFrameHolder = new SimplePanel();
            // videoFrameHolder.add(videoFrame); 

            VerticalPanel rootPanel = new VerticalPanel();
            if (showSynchControls) {
                
                mediaSynchControl = new MediaSynchControl(this);
                rootPanel.add(mediaSynchControl.widget());
            }
            rootPanel.add(mediaControl);
            dialogBox.setWidget(rootPanel);
        }
        
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

//    @Override
//    protected void onMediaTimeUpdate() {
//        if (mediaSynchControl != null) {
//            long currentMediaTime = getMediaTrack().startTime.getTime() + getCurrentMediaTimeMillis();
//            long currentRaceTime = raceTimer.getTime().getTime();
//            long currentOffset = currentMediaTime - currentRaceTime;
//            mediaSynchControl.offsetChanged(currentOffset);
//        }
//        
//    }
    
    @Override
    public void pauseRace() {
        raceTimer.pause();
    }

}
