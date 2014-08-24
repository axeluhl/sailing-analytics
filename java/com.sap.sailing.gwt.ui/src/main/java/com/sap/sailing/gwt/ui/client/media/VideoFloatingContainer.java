package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PopoutCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sse.gwt.client.dialog.WindowBox;

public class VideoFloatingContainer implements VideoContainer {

    private final WindowBox dialogBox;
    private  final MediaSynchControl mediaSynchControl;

    private final PopoutCloseListener popupCloseListener;
    private final PopoutListener popoutListener;
    private final VideoSynchPlayer videoPlayer;

    public VideoFloatingContainer(VideoSynchPlayer videoPlayer, boolean showSynchControls, MediaServiceAsync mediaService, ErrorReporter errorReporter, PopoutCloseListener popCloseListener, PopoutListener popoutListener) {
        this.videoPlayer = videoPlayer;
        this.popoutListener = popoutListener;
        
        this.popupCloseListener = popCloseListener;
        
        FlowPanel rootPanel = new FlowPanel();
        rootPanel.addStyleName("video-root-panel");
        rootPanel.add(videoPlayer.getWidget());
        
        if (showSynchControls) {                
            mediaSynchControl = new MediaSynchControl(this.videoPlayer, mediaService, errorReporter);
            mediaSynchControl.widget().addStyleName("media-synch-control");

            rootPanel.add(mediaSynchControl.widget());
        } else {
            mediaSynchControl = null;
        }
            
        videoPlayer.setEditFlag(mediaSynchControl);

        this.dialogBox = new WindowBox(videoPlayer.getMediaTrack().title, videoPlayer.getMediaTrack().toString(), rootPanel, new WindowBox.PopoutHandler() {
            
            @Override
            public void popout() {
                VideoFloatingContainer.this.popoutListener.popoutVideo(VideoFloatingContainer.this.videoPlayer.getMediaTrack());
                
            }
        });        
        dialogBox.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                VideoFloatingContainer.this.videoPlayer.pauseMedia();
                VideoFloatingContainer.this.popupCloseListener.popoutClosed();
            }
            
        });
        
        show();
    }
    
    void show() {
        dialogBox.show();
    }
    
    @Override
    public void shutDown() {
        videoPlayer.shutDown();
      dialogBox.hide();
    }

    @Override
    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

}
