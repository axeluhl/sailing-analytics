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
import com.sap.sailing.domain.common.media.MediaTrack;

public class MediaSingleSelectionControl extends AbstractMediaSelectionControl implements CloseHandler<PopupPanel> {

    private final DialogBox dialogControl;
    private final UIObject popupLocation;

    public MediaSingleSelectionControl(MediaPlayerManager mediaPlayerManager, UIObject popupLocation) {
        super(mediaPlayerManager);
        this.popupLocation = popupLocation;

        this.dialogControl = new DialogBox(true, false);
        this.dialogControl.addStyleName("Media-Select-Popup");
        this.dialogControl.setText("Select Video");
        this.dialogControl.addCloseHandler(this);

    }

    public void show() {
        Collection<MediaTrack> mediaTracks = new ArrayList<MediaTrack>();
        addPotentiallyPlayableMediaTracksTo(mediaTracks);
        Panel mediaPanel = new VerticalPanel();
        addMediaEntriesToGridPanel(mediaTracks, mediaPanel);
        dialogControl.add(mediaPanel);
        dialogControl.showRelativeTo(popupLocation);
    }

    private void addMediaEntriesToGridPanel(Collection<MediaTrack> mediaTracks, Panel mediaPanel) {
        for (MediaTrack videoTrack : mediaTracks) {
            mediaPanel.add(createMediaEntry(videoTrack));
        }
    }

    private void addPotentiallyPlayableMediaTracksTo(Collection<MediaTrack> mediaTracks) {
        for (MediaTrack mediaTrack : mediaPlayerManager.getAssignedMediaTracks()) {
            if (isPotentiallyPlayable(mediaTrack)) {
                mediaTracks.add(mediaTrack);
            }
        }
    }

    private Button createMediaEntry(final MediaTrack mediaTrack) {
        Button mediaSelectButton = new Button(mediaTrack.title);
        mediaSelectButton.setStyleName("Media-Select-Button");
        if (mediaPlayerManager.getPlayingVideoTracks().contains(mediaTrack)) {
            mediaSelectButton.addStyleName("Media-Select-Button-playing");
            mediaSelectButton.setEnabled(false);
        } else {
            mediaSelectButton.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    if (mediaPlayerManager.getPlayingAudioTrack() != mediaTrack) {
                        mediaPlayerManager.stopAll();
                        if (mediaTrack.mimeType.mediaType == MediaTrack.MediaType.video) {
                            mediaPlayerManager.playFloatingVideo(mediaTrack);
                            mediaPlayerManager.playAudio(mediaTrack);
                        } else if (mediaTrack.mimeType.mediaType == MediaTrack.MediaType.audio) {
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
