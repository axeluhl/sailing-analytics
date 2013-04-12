package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class MediaSelectionDialog implements CloseHandler<PopupPanel> {
    
    static interface MediaSelectionListener {
        
        void audioChanged(MediaTrack audioTrack);
        
        void videoSelected(MediaTrack videoTrack);
        
        void videoDeselected(MediaTrack videoTrack);
        
    }
    
    private final DialogBox dialogControl;
    private final MediaSelectionListener mediaSelectionListener;
    private final Map<MediaTrack, CheckBox> videoCheckBoxes = new HashMap<MediaTrack, CheckBox>();
    
    MediaSelectionDialog(MediaSelectionListener mediaSelectionListener) {
        this.mediaSelectionListener = mediaSelectionListener;
        dialogControl = new DialogBox(true, false);
        dialogControl.setText("Select Playback Media");
        dialogControl.addCloseHandler(this);
    }
    
    public void show(Collection<MediaTrack> videoTracks, Set<MediaTrack> selectedVideos, Collection<MediaTrack> audioTracks, MediaTrack selectedAudioTrack, UIObject popupLocation) {
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
                    mediaSelectionListener.videoSelected(videoTrack);
                } else {
                    mediaSelectionListener.videoDeselected(videoTrack);
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
        String label = audioTrack != null ? audioTrack.title: "Sound off";
        String title = audioTrack != null ? audioTrack.toString() : "Turn off all sound channels.";
        RadioButton audioButton = new RadioButton("group-name", label);
        audioButton.setTitle(title);
        audioButton.setValue(audioTrack == selectedAudioTrack);
        audioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    mediaSelectionListener.audioChanged(audioTrack);
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

}

