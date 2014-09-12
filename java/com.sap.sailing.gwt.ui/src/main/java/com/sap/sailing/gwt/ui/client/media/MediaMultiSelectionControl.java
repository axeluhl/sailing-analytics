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
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;

public class MediaMultiSelectionControl extends AbstractMediaSelectionControl implements CloseHandler<PopupPanel> {

    private final DialogBox dialogControl;
    private final Map<MediaTrack, CheckBox> videoCheckBoxes = new HashMap<MediaTrack, CheckBox>();
    private final UIObject popupLocation;

    public MediaMultiSelectionControl(MediaPlayerManager mediaPlayerManager, UIObject popupLocation) {
        super(mediaPlayerManager);
        this.popupLocation = popupLocation;

        this.dialogControl = new DialogBox(true, false);
        this.dialogControl.setText("Select Playback Media");
        this.dialogControl.addCloseHandler(this);

    }

    public void show() {
        Collection<MediaTrack> reachableVideoTracks = new ArrayList<MediaTrack>();
        Collection<MediaTrack> reachableAudioTracks = new ArrayList<MediaTrack>();
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
        if (mediaPlayerManager.allowsEditing()) {
            Button addButton = new Button("Add", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    hide();
                    mediaPlayerManager.addMediaTrack();
                }
            });
//            controlButtons.setWidget(0, 0, addButton);
            controlButtons.add(addButton);
        }

        Button closeButton = new Button("Close", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
//        controlButtons.setWidget(0, 1, closeButton);
        controlButtons.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
        controlButtons.add(closeButton);
        
        grid.add(controlButtons);
//        grid.set
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
            if (isPotentiallyPlayable(mediaTrack)) {
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
                }
            }
        }
    }

    private void addAssignedMediaTracksTo(Collection<MediaTrack> reachableVideoTracks,
            Collection<MediaTrack> reachableAudioTracks) {
        for (MediaTrack mediaTrack : mediaPlayerManager.getAssignedMediaTracks()) {
            if (isPotentiallyPlayable(mediaTrack)) {
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

            Button deleteButton = createDeleteButton(videoTrack, panel);
            panel.setCellHorizontalAlignment(deleteButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.add(deleteButton);
            CheckBox connectCheckBox = createConnectCheckBox(videoTrack);
            panel.setCellHorizontalAlignment(deleteButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.add(connectCheckBox);      
            setEnableOfVideoTrack(connectCheckBox, connectCheckBox.getValue());
            return panel;
        } else {
            return playCheckBox;
        }

    }

    private CheckBox createConnectCheckBox(final MediaTrack videoTrack) {
        CheckBox connectCheckBox = new CheckBox();
        connectCheckBox.setValue(videoTrack.regattasAndRaces
                .contains(((MediaPlayerManagerComponent) mediaPlayerManager).getRaceIdentifier()));
        connectCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    connectVideoToRace(videoTrack);
                    setEnableOfVideoTrack((CheckBox)changeEvent.getSource(), true);
                } else {
                    disconnectVideoFromRace(videoTrack);
                    setEnableOfVideoTrack((CheckBox)changeEvent.getSource(), false);
                }
            }
        });
        return connectCheckBox;
    }
    
    private void setEnableOfVideoTrack(CheckBox checkBox, boolean enable) {
        Panel videoTrack = (HorizontalPanel)checkBox.getParent();
        for (Widget widget : videoTrack) {
            if(widget != checkBox && widget instanceof FocusWidget){
                ((FocusWidget)widget).setEnabled(enable);
            }
        }
    }
    
    private void disconnectVideoFromRace(final MediaTrack videoTrack) {
        videoTrack.regattasAndRaces.remove(((MediaPlayerManagerComponent)mediaPlayerManager).getRaceIdentifier());
        ((MediaPlayerManagerComponent)mediaPlayerManager).getMediaService().updateRace(videoTrack, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
            }

            @Override
            public void onSuccess(Void allMediaTracks) {
            }
        });
        
    }

    private void connectVideoToRace(final MediaTrack videoTrack) {
        videoTrack.regattasAndRaces.add(((MediaPlayerManagerComponent)mediaPlayerManager).getRaceIdentifier());
        ((MediaPlayerManagerComponent)mediaPlayerManager).getMediaService().updateRace(videoTrack, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
            }

            @Override
            public void onSuccess(Void allMediaTracks) {
            }
        });
    }

    private Button createDeleteButton(final MediaTrack videoTrack, HorizontalPanel panel) {
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
        if (mediaPlayerManager.hasLoadedAllMediaTracks()) {

        }
    }

}
