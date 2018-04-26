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
import com.google.gwt.user.client.ui.FocusWidget;
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
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;

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
        Collection<MediaTrack> reachableVideoTracks = new ArrayList<>();
        Collection<MediaTrack> reachableAudioTracks = new ArrayList<>();
        addAssignedMediaTracksTo(reachableVideoTracks, reachableAudioTracks);
        addOverlappingMediaTracksTo(reachableVideoTracks, reachableAudioTracks);
        Panel grid = new VerticalPanel();
        addAudioTracksToGridPanel(reachableAudioTracks, grid);
        addVideoTracksToGridPanel(reachableVideoTracks, grid);
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

    private void addVideoTracksToGridPanel(Collection<MediaTrack> reachableVideoTracks, Panel grid) {
        if (!reachableVideoTracks.isEmpty()) {
            grid.add(createVideoHeader());
            for (MediaTrack videoTrack : reachableVideoTracks) {
                grid.add(createVideoOptions(videoTrack, mediaPlayerManager.getPlayingVideoTracks()));
            }
        }
    }

    private void addAudioTracksToGridPanel(Collection<MediaTrack> reachableAudioTracks, Panel grid) {
        if (!reachableAudioTracks.isEmpty()) {
            grid.add(createAudioHeader());
            grid.add((RadioButton) createAudioButton(null, mediaPlayerManager.getPlayingAudioTrack()));
            for (MediaTrack audioTrack : reachableAudioTracks) {
                grid.add(createAudioButton(audioTrack, mediaPlayerManager.getPlayingAudioTrack()));
            }
        }
    }

    private void addOverlappingMediaTracksTo(Collection<MediaTrack> reachableVideoTracks,
            Collection<MediaTrack> reachableAudioTracks) {
        for (MediaTrack mediaTrack : mediaPlayerManager.getOverlappingMediaTracks()) {
            if (isPotentiallyPlayable(mediaTrack) && mediaTrack.mimeType != null) {
                switch (mediaTrack.mimeType.mediaType) {
                case video:
                    reachableVideoTracks.add(mediaTrack);
                case audio: // intentional fall through
                    if (mediaPlayerManager.getUserAgent().getType().equals(AgentTypes.FIREFOX)) {
                        if (mediaTrack.isYoutube()) {
                            // only youtube audio tracks work with firefox
                            reachableAudioTracks.add(mediaTrack);
                        }
                    } else {
                        reachableAudioTracks.add(mediaTrack);
                    }
                case image: // no image media tracks produced by an image
                    break;
                case unknown: // we won't overlay an unknown media source
                    break;
                default:
                    break;
                }
            }
        }
    }

    private void addAssignedMediaTracksTo(Collection<MediaTrack> reachableVideoTracks,
            Collection<MediaTrack> reachableAudioTracks) {
        for (MediaTrack mediaTrack : mediaPlayerManager.getAssignedMediaTracks()) {
            if (isPotentiallyPlayable(mediaTrack)) {
                if (mediaTrack.mimeType == null) {
                    reachableVideoTracks.add(mediaTrack); // allow user to remove this strange artifact
                } else {
                    switch (mediaTrack.mimeType.mediaType) {
                    case video:
                        reachableVideoTracks.add(mediaTrack);
                    case audio: // intentional fall through
                        if (mediaPlayerManager.getUserAgent().getType().equals(AgentTypes.FIREFOX)) {
                            if (mediaTrack.isYoutube()) {
                                // only youtube audio tracks work with firefox
                                reachableAudioTracks.add(mediaTrack);
                            }
                        } else {
                            reachableAudioTracks.add(mediaTrack);
                        }
                    case image: // images don't play as video tracks
                        break;
                    case unknown: // unknown formats won't be played either
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    private Widget createVideoOptions(final MediaTrack videoTrack, Set<MediaTrack> selectedVideos) {
        CheckBox playCheckBox = createPlayCheckBox(videoTrack, selectedVideos);

        if (mediaPlayerManager.allowsEditing()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setWidth("100%");
            panel.add(playCheckBox);

            Button deleteButton = createDeleteButton(videoTrack);
            panel.add(deleteButton);
            ToggleButton connectButton = createConnectButton(videoTrack);
            panel.setCellHorizontalAlignment(deleteButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.add(connectButton);   
            panel.setCellHorizontalAlignment(connectButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.setCellWidth(connectButton, "13");
            setEnableOfVideoTrack(connectButton, connectButton.getValue());
            return panel;
        } else {
            return playCheckBox;
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
    
    private void setEnableOfVideoTrack(ToggleButton connectButton, boolean enable) {
        Panel videoTrack = (HorizontalPanel)connectButton.getParent();
        for (Widget widget : videoTrack) {
            if(widget != connectButton && widget instanceof FocusWidget){
                ((FocusWidget)widget).setEnabled(enable);
                if(widget instanceof CheckBox){
                    ((CheckBox)widget).setValue(false);
                }
            }
        }
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
                setEnableOfVideoTrack(connectButton, false);
                mediaPlayerManager.closeFloatingVideo(videoTrack);
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
                setEnableOfVideoTrack(connectButton, true);
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

    private CheckBox createPlayCheckBox(final MediaTrack videoTrack, Set<MediaTrack> selectedVideos) {
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
                    mediaPlayerManager.closeFloatingVideo(videoTrack);
                }
            }
        });
        return videoCheckBox;
    }

    public void selectVideo(MediaTrack videoTrack) {
        CheckBox videoCheckBox = videoCheckBoxes.get(videoTrack);
        if (videoCheckBox != null) {
            videoCheckBox.setValue(true);
        }
    }

    public void unselectVideo(MediaTrack videoTrack) {
        CheckBox videoCheckBox = videoCheckBoxes.get(videoTrack);
        if (videoCheckBox != null) {
            videoCheckBox.setValue(false);
        }
    }

    private Widget createVideoHeader() {
        Label audioHeader = new Label("Videos");
        return audioHeader;
    }

    private Widget createAudioButton(final MediaTrack audioTrack, MediaTrack selectedAudioTrack) {
        String label = audioTrack != null ? audioTrack.title : "Sound off";
        String title = audioTrack != null ? audioTrack.toString() : "Turn off all sound channels.";
        RadioButton audioButton = new RadioButton("group-name", label);
        audioButton.setTitle(title);
        audioButton.setValue(audioTrack == selectedAudioTrack);
        audioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    mediaPlayerManager.playAudio(audioTrack);
                }
            }
        });
        return audioButton;
    }

    private Widget createAudioHeader() {
        Label audioHeader = new Label("Audio Tracks");
        return audioHeader;
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
