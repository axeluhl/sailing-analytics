package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.client.PlayStateListener;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.raceboard.MediaSelectionDialog.MediaSelectionListener;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class MediaSelector implements RaceTimesInfoProviderListener, PlayStateListener, TimeListener,
        AsyncCallback<Collection<MediaTrack>>, MediaSelectionListener {

    private final Button manageMediaButton;
    private final MediaSelectionDialog mediaSelectionDialog;

    private final AudioControl audioPlayer;
    private final Map<MediaTrack, VideoPopup> videoPlayers = new HashMap<MediaTrack, VideoPopup>();
    private final Collection<MediaTrack> audioTracks = new ArrayList<MediaTrack>();
    private final Collection<MediaTrack> videoTracks = new ArrayList<MediaTrack>();
    private final ErrorReporter errorReporter;

    public MediaSelector(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        
        mediaSelectionDialog = new MediaSelectionDialog(this);         

        audioPlayer = new AudioControl();
        
        manageMediaButton = new Button("Audio & Video");
        manageMediaButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                mediaSelectionDialog.show(videoTracks, audioTracks, manageMediaButton);
            }
        });
        manageMediaButton.addStyleName("raceBoardNavigation-standaloneElement");
        manageMediaButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        manageMediaButton.setTitle("Configure Media");


    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        if (PlayModes.Replay.equals(playMode)) {
            switch (playState) {
            case Playing:
                startPlaying();
                break;
            case Paused:
            case Stopped:
                pausePlaying();
            default:
                break;
            }
        } else {
            // TODO
        }
    }

    private void updatePlayState() {
        
    }

    private void pausePlaying() {
        audioPlayer.pause();
        for (MediaPlayer player : videoPlayers.values()) {
            if (!player.isPaused()) {
                player.pause();
            }
        }
    }

    private void startPlaying() {
        audioPlayer.play();
        for (MediaPlayer player : videoPlayers.values()) {
            if (player.isPaused()) {
                player.play();
            }
        }
    }

    @Override
    public void timeChanged(Date raceTime) {
        audioPlayer.alignTime(raceTime);
        for (MediaPlayer player : videoPlayers.values()) {
            player.alignTime(raceTime);
        }
    }

    public Widget widget() {
        return manageMediaButton;
    }

    @Override
    public void onFailure(Throwable caught) {
        errorReporter
                .reportError("Remote Procedure Call getPreviousConfigurations() - Failure: " + caught.getMessage());
    }

    @Override
    public void onSuccess(Collection<MediaTrack> mediaTracks) {
        manageMediaButton.setEnabled(mediaTracks.size() > 0);
        for (MediaTrack mediaTrack : mediaTracks) {
            switch (mediaTrack.type) {
            case AUDIO:
                audioTracks.add(mediaTrack);
                break;
            case VIDEO:
                videoTracks.add(mediaTrack);
                break;
            }
        }
    }

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void audioChanged(MediaTrack audioTrack) {
        audioPlayer.setMediaTrack(audioTrack);
        updatePlayState();
    }

    @Override
    public void videoSelected(MediaTrack videoTrack) {
        if (!videoPlayers.containsKey(videoTrack)) {
            final VideoPopup videoPopup = new VideoPopup();
            videoPlayers.put(videoTrack, videoPopup);
        }
        updatePlayState();
    }

    @Override
    public void videoDeselected(MediaTrack videoTrack) {
        VideoPopup removedVideoPlayer = videoPlayers.remove(videoTrack);
        removedVideoPlayer.pause();
        removedVideoPlayer.hide(false);
        updatePlayState();
    }

}
