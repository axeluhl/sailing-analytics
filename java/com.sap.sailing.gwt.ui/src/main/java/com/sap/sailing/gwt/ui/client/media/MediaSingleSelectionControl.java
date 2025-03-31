package com.sap.sailing.gwt.ui.client.media;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.media.MediaType;

public class MediaSingleSelectionControl extends AbstractMediaSelectionControl implements CloseHandler<PopupPanel> {

    private final DialogBox dialogControl;
    private final UIObject popupLocation;

    public MediaSingleSelectionControl(MediaPlayerManager mediaPlayerManager, UIObject popupLocation, StringMessages stringMessages) {
        super(mediaPlayerManager, stringMessages);
        this.popupLocation = popupLocation;

        this.dialogControl = new DialogBox(true, false);
        this.dialogControl.addStyleName("Media-Select-Popup");
        this.dialogControl.setText(stringMessages.selectMedia());
        this.dialogControl.addCloseHandler(this);

    }

    public void show() {
        final Collection<MediaTrackWithSecurityDTO> mediaTracks = new ArrayList<>(
                mediaPlayerManager.getAssignedMediaTracks());
        Panel mediaPanel = new VerticalPanel();
        mediaTracks.forEach(track -> mediaPanel.add(createMediaEntry(track)));
        dialogControl.add(mediaPanel);
        dialogControl.showRelativeTo(popupLocation);
    }

    private Button createMediaEntry(final MediaTrackWithSecurityDTO mediaTrack) {
        Button mediaSelectButton = new Button(
                mediaPlayerManager.getMediaTrackStatus(mediaTrack).toString() + " " + mediaTrack.title);
        mediaSelectButton.setStyleName("Media-Select-Button");
        if (mediaPlayerManager.getPlayingAudioTrack().contains(mediaTrack)
                || mediaPlayerManager.getPlayingVideoTracks().contains(mediaTrack)) {
            mediaSelectButton.setTitle(stringMessages.mediaHideVideoTooltip());
            mediaSelectButton.addStyleName("Media-Select-Button-playing");
            mediaSelectButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (mediaPlayerManager.getPlayingAudioTrack() == mediaTrack) {
                        mediaPlayerManager.muteAudio();
                    }
                    mediaPlayerManager.closeFloatingPlayer(mediaTrack);
                    hide();
                };
            });
        } else {
            mediaSelectButton.setTitle(stringMessages.mediaShowVideoTooltip(mediaTrack.title));
            mediaSelectButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (mediaTrack.mimeType != null) {
                        if (mediaTrack.mimeType.mediaType == MediaType.video) {
                            mediaPlayerManager.playFloatingVideo(mediaTrack);
                        }
                        if (mediaTrack.mimeType.mediaType == MediaType.audio || mediaPlayerManager.getPlayingAudioTrack() == null) {
                            mediaPlayerManager.playAudio(mediaTrack);
                        }
                    }
                    hide();
                };
            });
        }
        return mediaSelectButton;
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> arg0) {
        dialogControl.clear();
    }

    public boolean isShowing() {
        return dialogControl.isShowing();
    }

    public void hide() {
        dialogControl.hide(false);
    }

    @Override
    protected void updateUi() {
    }

}
