package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.WindowBox;

public class VideoFloatingContainer extends AbstractVideoContainer implements VideoContainer {

    private static final int MIN_WIDTH = 420;
    private final WindowBox dialogBox;
    private final MediaSynchControl mediaSynchControl;
    private final PopupPositionProvider popupPositionProvider;
    
    public VideoFloatingContainer(VideoSynchPlayer videoPlayer, PopupPositionProvider popupPositionProvider, boolean showSynchControls, MediaServiceAsync mediaService, ErrorReporter errorReporter, PlayerCloseListener playerCloseListener, PopoutListener popoutListener) {
        super(new FlowPanel(), videoPlayer, popoutListener, playerCloseListener);

        this.popupPositionProvider = popupPositionProvider;
        
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
        //values required for js and youtube player to stay useable
        dialogBox.setMinWidth(MIN_WIDTH);
        dialogBox.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                VideoFloatingContainer.this.videoPlayer.pauseMedia();
                VideoFloatingContainer.this.popupCloseListener.playerClosed();
            }

        });

        show();
    }

    @Override
    void show() {
        dialogBox.show();
        dialogBox.setVisible(false);
        
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                int absoluteTop = popupPositionProvider.getYPositionUiObject().getAbsoluteTop();
                int posY = absoluteTop - dialogBox.getOffsetHeight() - 40;
                dialogBox.setPopupPosition(5, posY);
                dialogBox.setPixelSize(videoPlayer.getDefaultWidth(), videoPlayer.getDefaultHeight());
                dialogBox.setVisible(true);
            }
        });

    }

    @Override
    void hide() {
        dialogBox.hide();
    }

}
