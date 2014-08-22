package com.sap.sailing.gwt.ui.client.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
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

public class MediaMultiSelectionControl extends AbstractMediaSelectionControl implements MediaPlayerManager.PlayerChangeListener, CloseHandler<PopupPanel> {

    private final DialogBox dialogControl;
    private final Map<MediaTrack, CheckBox> videoCheckBoxes = new HashMap<MediaTrack, CheckBox>();
    
    private final CheckBox toggleMediaButton;
    private final Button selectMediaButton;

    public MediaMultiSelectionControl(MediaPlayerManager mediaPlayerManager, AgentTypes userAgent) {
        super(mediaPlayerManager, userAgent);

        this.dialogControl = new DialogBox(true, false);
        this.dialogControl.setText("Select Playback Media");
        this.dialogControl.addCloseHandler(this);

        this.selectMediaButton = new Button();
        this.selectMediaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isShowing()) {
                    hide();
                } else {
                    showSelectionDialog();
                }
            }
        });
        selectMediaButton.addStyleName("raceBoardNavigation-settingsButton");
        selectMediaButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        selectMediaButton.setTitle("Configure Media");

        toggleMediaButton = new CheckBox("Audio & Video");
        toggleMediaButton.addStyleName("raceBoardNavigation-innerElement");
        toggleMediaButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        toggleMediaButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (toggleMediaButton.getValue()) {
                    MediaMultiSelectionControl.this.mediaPlayerManager.playDefault();
                } else {
                    MediaMultiSelectionControl.this.mediaPlayerManager.stopAll();
                }

            }

        });
        setWidgetsVisible(false);

    }

    private void setWidgetsVisible(boolean isVisible) {
        selectMediaButton.setVisible(isVisible);
        toggleMediaButton.setVisible(isVisible);
    }

    protected void updateUi() {
        setWidgetsVisible((mediaPlayerManager.getMediaTracks().size() > 0) || mediaPlayerManager.allowsEditing());
        toggleMediaButton.setValue(mediaPlayerManager.isPlaying());
    }

    private void showSelectionDialog() {
        MediaTrack playingAudioTrack = mediaPlayerManager.getPlayingAudioTrack();
        Set<MediaTrack> playingVideoTracks = mediaPlayerManager.getPlayingVideoTracks();

        Collection<MediaTrack> reachableVideoTracks = new ArrayList<MediaTrack>();
        Collection<MediaTrack> reachableAudioTracks = new ArrayList<MediaTrack>();
        for (MediaTrack mediaTrack : mediaPlayerManager.getMediaTracks()) {
            if (isPotentiallyPlayable(mediaTrack)) {
                switch (mediaTrack.mimeType.mediaType) {
                case video:
                    reachableVideoTracks.add(mediaTrack);
                case audio: // intentional fall through
                    if(userAgent.equals(AgentTypes.FIREFOX)) {
                        if(mediaTrack.isYoutube()) {
                            // only youtube audio tracks work with firefox
                            reachableAudioTracks.add(mediaTrack);
                        }
                    } else {
                        reachableAudioTracks.add(mediaTrack);
                    }
                }
            }
        }

        boolean showAddButton = mediaPlayerManager.allowsEditing();
        show(reachableVideoTracks, playingVideoTracks, reachableAudioTracks,
                playingAudioTrack, showAddButton, toggleMediaButton);
    }

    public void show(Collection<MediaTrack> videoTracks, Set<MediaTrack> selectedVideos, Collection<MediaTrack> audioTracks, MediaTrack selectedAudioTrack, boolean showAddButton, UIObject popupLocation) {
        Panel grid = new VerticalPanel();
        if (!audioTracks.isEmpty()) {
            grid.add(createAudioHeader());
            grid.add((RadioButton) createAudioButton(null, selectedAudioTrack));
            for (MediaTrack audioTrack : audioTracks) {
                grid.add(createAudioButton(audioTrack, selectedAudioTrack));
            }
        }
        if (!videoTracks.isEmpty()) {
            grid.add(createVideoHeader());
            for (MediaTrack videoTrack : videoTracks) {
                grid.add(createVideoCheckBox(videoTrack, selectedVideos));
            }
        }
        if (mediaPlayerManager.allowsEditing()) {
            Button addButton = new Button("Add", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                    mediaPlayerManager.addMediaTrack();
                }
            });
            grid.add(addButton);
        }
        dialogControl.add(grid);
        dialogControl.showRelativeTo(popupLocation);
    }

    private Widget createVideoCheckBox(final MediaTrack videoTrack, Set<MediaTrack> selectedVideos) {
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
        
        if (mediaPlayerManager.allowsEditing()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setWidth("100%");
            panel.add(videoCheckBox);
            
            Button deleteButton = new Button("X", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    if (mediaPlayerManager.deleteMediaTrack(videoTrack)) {
                        hide();
                    }
                }
            });
            panel.setCellHorizontalAlignment(deleteButton, HasHorizontalAlignment.ALIGN_RIGHT);
            panel.add(deleteButton);
            
            return panel;
        } else {
            return videoCheckBox;
        }
        
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
        String label = audioTrack != null ? audioTrack.title: "Sound off";
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

    public Widget[] widgets() {
        return new Widget[] {toggleMediaButton, selectMediaButton};
    }

}
