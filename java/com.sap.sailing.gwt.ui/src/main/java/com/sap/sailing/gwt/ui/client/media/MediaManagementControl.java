package com.sap.sailing.gwt.ui.client.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MediaManagementControl extends AbstractMediaSelectionControl implements CloseHandler<PopupPanel> {

    private final DialogBox dialogControl;
    private final Map<MediaTrack, CheckBox> videoCheckBoxes = new HashMap<MediaTrack, CheckBox>();
    private final UIObject popupLocation;

    public MediaManagementControl(MediaPlayerManager mediaPlayerManager, UIObject popupLocation, StringMessages stringMessages) {
        super(mediaPlayerManager, stringMessages);
        this.popupLocation = popupLocation;
        this.dialogControl = new DialogBox(true, false);
        this.dialogControl.setText(stringMessages.managePlaybackMedia());
        this.dialogControl.addCloseHandler(this);
    }

    public void show() {
        Collection<MediaTrack> videoTracks = new ArrayList<>();
        Collection<MediaTrack> audioTracks = new ArrayList<>();
        filterGivenMediaTracksTo(videoTracks, audioTracks, mediaPlayerManager.getAssignedMediaTracks());
        filterGivenMediaTracksTo(videoTracks, audioTracks, mediaPlayerManager.getOverlappingMediaTracks());
        Panel grid = new VerticalPanel();
        addAudioTracksToGridPanel(audioTracks, grid);
        addVideoTracksToGridPanel(videoTracks, grid);
        addNewMediaButtonsTo(grid);
        dialogControl.add(grid);
        dialogControl.showRelativeTo(popupLocation);
    }

    private void addNewMediaButtonsTo(Panel grid) {
        HorizontalPanel controlButtons = new HorizontalPanel();
        controlButtons.setWidth("100%");
        if (mediaPlayerManager.allowsEditing()) {
            Button addButton = new Button(stringMessages.add(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                    mediaPlayerManager.addMediaTrack();
                }
            });
            controlButtons.add(addButton);
        }
        Button closeButton = new Button(stringMessages.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        controlButtons.add(closeButton);
        controlButtons.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
        grid.add(controlButtons);
    }

    private void addVideoTracksToGridPanel(Collection<MediaTrack> videoTracks, Panel grid) {
        if (!videoTracks.isEmpty()) {
            grid.add(new Label(stringMessages.videos()));
            for (MediaTrack videoTrack : videoTracks) {
                grid.add(createVideoOptions(videoTrack, mediaPlayerManager.getPlayingVideoTracks()));
            }
        }
    }

    private void addAudioTracksToGridPanel(Collection<MediaTrack> audioTracks, Panel grid) {
        if (!audioTracks.isEmpty()) {
            grid.add(new Label(stringMessages.audioFiles()));
            grid.add(createAudioButton(null));
            for (MediaTrack audioTrack : audioTracks) {
                grid.add(createAudioButton(audioTrack));
            }
        }
    }

    private void filterGivenMediaTracksTo(Collection<MediaTrack> videoTracks,
            Collection<MediaTrack> audioTracks, Iterable<MediaTrack> source) {
        for (MediaTrack mediaTrack : source) {
            if (mediaTrack.mimeType == null) {
                videoTracks.add(mediaTrack); // allow user to remove this strange artifact
            } else {
                switch (mediaTrack.mimeType.mediaType) {
                case video:
                    videoTracks.add(mediaTrack);
                    break;
                case audio:
                    audioTracks.add(mediaTrack);
                    break;
                default:
                    break;
                }
            }
        }
    }

    private Widget createVideoOptions(final MediaTrack videoTrack, Set<MediaTrack> selectedVideos) {
        CheckBox videoCheckBox = new CheckBox(videoTrack.title);
        videoCheckBoxes.put(videoTrack, videoCheckBox);
        videoCheckBox.setTitle(videoTrack.toString());
        videoCheckBox.setValue(selectedVideos.contains(videoTrack));
        videoCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    mediaPlayerManager.playFloatingVideo(videoTrack);
                } else {
                    mediaPlayerManager.closeFloatingPlayer(videoTrack);
                }
            }
        });

        if (mediaPlayerManager.allowsEditing()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setWidth("100%");
            panel.add(videoCheckBox);

            Button deleteButton = createDeleteButton(videoTrack);
            panel.add(deleteButton);
            ToggleButton connectButton = createConnectButton(videoTrack);
            panel.setCellHorizontalAlignment(deleteButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.add(connectButton);   
            panel.setCellHorizontalAlignment(connectButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.setCellWidth(connectButton, "13");
            setVideoPlayCheckboxEnabled(videoTrack, connectButton.getValue());
            return panel;
        } else {
            return videoCheckBox;
        }

    }

    private ToggleButton createConnectButton(final MediaTrack videoTrack) {
        final ToggleButton connectButton = new ToggleButton();
        connectButton.setValue(videoTrack.assignedRaces
                .contains(mediaPlayerManager.getCurrentRace()));
        connectButton.setTitle("Connect video to this race");
        connectButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (connectButton.isDown()) {
                    connectVideoToRace(videoTrack, connectButton);
                } else {
                    disconnectVideoFromRace(videoTrack, connectButton);
                }
            }
        });
        return connectButton;
    }
    
    private void setVideoPlayCheckboxEnabled(MediaTrack videoTrack, boolean enable) {
        CheckBox videoCheckBox = videoCheckBoxes.get(videoTrack);
        videoCheckBox.setEnabled(enable);
    }
    
    private void disconnectVideoFromRace(final MediaTrack videoTrack, final ToggleButton connectButton) {
        videoTrack.assignedRaces.remove(mediaPlayerManager.getCurrentRace());
        mediaPlayerManager.getMediaService().updateRace(videoTrack, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
                mediaPlayerManager.getErrorReporter().reportError(t.toString());
            }

            @Override
            public void onSuccess(Void allMediaTracks) {
                setVideoPlayCheckboxEnabled(videoTrack, false);
                mediaPlayerManager.closeFloatingPlayer(videoTrack);
            }
        });
        
    }

    private void connectVideoToRace(final MediaTrack videoTrack, final ToggleButton connectButton) {
        videoTrack.assignedRaces.add(mediaPlayerManager.getCurrentRace());
        mediaPlayerManager.getMediaService().updateRace(videoTrack, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
                mediaPlayerManager.getErrorReporter().reportError(t.toString());
            }

            @Override
            public void onSuccess(Void allMediaTracks) {
                setVideoPlayCheckboxEnabled(videoTrack, true);
            }
        });
    }

    private Button createDeleteButton(final MediaTrack videoTrack) {
        Button deleteButton = new Button("X", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (mediaPlayerManager.deleteMediaTrack(videoTrack)) {
                    hide();
                }
            }
        });
        return deleteButton;
    }

    private Widget createAudioButton(final MediaTrack audioTrack) {
        String label = audioTrack != null ? audioTrack.title : "Sound off";
        String title = audioTrack != null ? audioTrack.toString() : "Turn off all sound channels.";
        RadioButton audioButton = new RadioButton("group-name", label);
        audioButton.setTitle(title);
        audioButton.setValue(audioTrack == mediaPlayerManager.getPlayingAudioTrack());
        audioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    Set<MediaTrack> currentlyPlaying = mediaPlayerManager.getPlayingAudioTrack();
                    if (audioTrack == null) {
                        currentlyPlaying.forEach(mediaPlayerManager::closeFloatingPlayer);
                    } else {
                        mediaPlayerManager.playAudio(audioTrack);
                    }
                }
            }
        });

        if (mediaPlayerManager.allowsEditing() && audioTrack != null) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setWidth("100%");
            panel.add(audioButton);

            Button deleteButton = createDeleteButton(audioTrack);
            panel.add(deleteButton);
            panel.setCellHorizontalAlignment(deleteButton, HasHorizontalAlignment.ALIGN_RIGHT);
            return panel;
        } else {
            return audioButton;
        }
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> arg0) {
        dialogControl.clear();
        videoCheckBoxes.clear();
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
