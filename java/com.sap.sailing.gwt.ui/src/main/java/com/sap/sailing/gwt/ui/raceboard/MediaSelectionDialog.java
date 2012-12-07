package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collection;
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
    
    MediaSelectionDialog(MediaSelectionListener mediaSelectionListener) {
        this.mediaSelectionListener = mediaSelectionListener;
        dialogControl = new DialogBox(true, true);
        dialogControl.setText("Select Playback Media");
        dialogControl.addCloseHandler(this);
    }

    public void show(Collection<MediaTrack> videoTracks, Set<MediaTrack> selectedVideos, Collection<MediaTrack> audioTracks, MediaTrack selectedAudioTrack, UIObject popupLocation) {
        Panel grid = new VerticalPanel();
        if (!audioTracks.isEmpty()) {
            grid.add(createAudioHeader());
            grid.add((RadioButton) createAudioItem(null, selectedAudioTrack));
            for (MediaTrack audioTrack : audioTracks) {
                grid.add(createAudioItem(audioTrack, selectedAudioTrack));
            }
        }
        if (!videoTracks.isEmpty()) {
            grid.add(createVideoHeader());
            for (MediaTrack videoTrack : videoTracks) {
                grid.add(createVideoItem(videoTrack, selectedVideos));
            }
        }
        dialogControl.add(grid);
        dialogControl.showRelativeTo(popupLocation);
    }

    private Widget createVideoItem(final MediaTrack videoTrack, Set<MediaTrack> selectedVideos) {
        CheckBox videoItem = new CheckBox(videoTrack.title);
        videoItem.setTitle(videoTrack.toString());
        videoItem.setValue(selectedVideos.contains(videoTrack));
        videoItem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    mediaSelectionListener.videoSelected(videoTrack);
                } else {
                    mediaSelectionListener.videoDeselected(videoTrack);
                }
            }
        });
        return videoItem;
    }

    private Widget createVideoHeader() {
        Label audioHeader = new Label("Videos");
        return audioHeader;
    }

    private Widget createAudioItem(final MediaTrack audioTrack, MediaTrack selectedAudioTrack) {
        String title = audioTrack != null ? audioTrack.title: "Mute - play no sound";
        String tooltip = audioTrack != null ? audioTrack.toString() : "Play no sound.";
        RadioButton audioItem = new RadioButton("group-name", title);
        audioItem.setTitle(tooltip);
        audioItem.setValue(audioTrack == selectedAudioTrack);
        audioItem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
                if (changeEvent.getValue()) {
                    mediaSelectionListener.audioChanged(audioTrack);
                }
            }
        });

        return audioItem;
    }

    private Widget createAudioHeader() {
        Label audioHeader = new Label("Audio Tracks");
        return audioHeader;
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

}

