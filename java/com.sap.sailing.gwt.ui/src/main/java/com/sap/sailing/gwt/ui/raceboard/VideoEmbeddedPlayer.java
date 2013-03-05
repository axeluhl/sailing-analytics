package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.shared.controls.dialog.WindowBox;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class VideoEmbeddedPlayer extends AbstractEmbeddedMediaPlayer implements VideoPlayer, CloseHandler<PopupPanel>, MediaSynchListener {

    private final WindowBox dialogBox;
    private final  PopupCloseListener popupCloseListener;
    private final long raceStartTimeMillis;
    private final MediaTrack backupVideoTrack;
    private final MediaServiceAsync mediaService;
    private final ErrorReporter errorReporter;

    public VideoEmbeddedPlayer(MediaTrack videoTrack, long raceStartTimeMillis, boolean showVideoSynch, MediaServiceAsync mediaService, MediaEventHandler mediaEventHandler, ErrorReporter errorReporter, PopupCloseListener popCloseListener) {
        super(videoTrack, mediaEventHandler);
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
            if (showVideoSynch) {
                
                MediaSynchControl mediaSynchControl = new MediaSynchControl(this);
                rootPanel.add(mediaSynchControl.widget());
            }
            rootPanel.add(mediaControl);
            dialogBox.setWidget(rootPanel);
        }
        
        dialogBox.addCloseHandler(this);
        show();
    }
    
    native void addNativeEventHandlers(VideoElement videoElement) /*-{
        var that = this;
        videoElement.addEventListener('timeupdate', function() {
            that.@com.sap.sailing.gwt.ui.raceboard.AbstractMediaPlayer::onMediaTimeUpdate()();
        });
    }-*/;

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
        pause();
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
    public void setOffset(long offset) {
        getMediaTrack().startTime = new Date(raceStartTimeMillis + offset);
        forceAlign();
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

}
