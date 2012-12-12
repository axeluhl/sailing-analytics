package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
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

    private final CheckBox toggleMediaButton;
    private final Button manageMediaButton;
    
    private final MediaSelectionDialog mediaSelectionDialog;

    private MediaPlayer audioPlayer;
    private final Map<MediaTrack, VideoPopup> videoPlayers = new HashMap<MediaTrack, VideoPopup>();
    private final Set<MediaTrack> audioTracks = new HashSet<MediaTrack>();
    private final Set<MediaTrack> videoTracks = new HashSet<MediaTrack>();
    private final ErrorReporter errorReporter;

    private Date currentRaceTime;
    private double currentPlaybackSpeed = 1.0d;
    private PlayStates currentPlayState = PlayStates.Paused;

    public MediaSelector(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        
        mediaSelectionDialog = new MediaSelectionDialog(this);         

        manageMediaButton = new Button();
        manageMediaButton.setEnabled(false);
        manageMediaButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (mediaSelectionDialog.isShowing()) {
                    mediaSelectionDialog.hide();
                } else {
                    MediaTrack playingAudioTrack = audioPlayer != null ? audioPlayer.getMediaTrack() : null;
                    Set<MediaTrack> playingVideoTracks = videoPlayers.keySet();
                    mediaSelectionDialog.show(videoTracks, playingVideoTracks, audioTracks, playingAudioTrack, toggleMediaButton);
                }
            }
        });
        manageMediaButton.addStyleName("raceBoardNavigation-settingsButton");
        manageMediaButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        manageMediaButton.setTitle("Configure Media");


        toggleMediaButton = new CheckBox("Audio & Video");
        toggleMediaButton.setEnabled(false);
        toggleMediaButton.addStyleName("raceBoardNavigation-innerElement");
        toggleMediaButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        toggleMediaButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (toggleMediaButton.getValue()) {
                    playDefault();
                } else {
                    stopAll();
                }

            }

        });
    }

    private void playDefault() {
            MediaTrack defaultVideo = getDefaultVideo();
            if (defaultVideo != null) {
                videoSelected(defaultVideo );
            }
            MediaTrack defaultAudio = getDefaultAudio();
            if (defaultAudio != null) {
                audioChanged(defaultAudio);
            }
    }

    private MediaTrack getDefaultAudio() {
        if (!audioTracks.isEmpty()) {
            return audioTracks.iterator().next();
        } else {
            //TODO: determine best fitting audio, e.g. best overlap with race
            return getDefaultVideo();
        }
    }

    private MediaTrack getDefaultVideo() {
        if (!videoTracks.isEmpty()) {
            return videoTracks.iterator().next();
        } else {
            //TODO: determine best fitting video, e.g. best overlap with race
            return null;
        }
        
    }

    private void stopAll() {
        if (audioPlayer != null) {
            audioPlayer.pause();
            audioPlayer = null;
        }
        for (VideoPopup videoPlayer : videoPlayers.values()) {
            videoPlayer.pause();
            videoPlayer.hide();
        }
        videoPlayers.clear();
    }

    
    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        this.currentPlayState = playState;
        if (PlayModes.Replay.equals(playMode)) {
            switch (this.currentPlayState) {
            case Playing:
                startPlaying();
                break;
            case Paused:
                //fall through to Stopped
            case Stopped:
                pausePlaying();
            default:
                break;
            }
        } else {
            // TODO: Live mode not supported, yet.
        }
    }

    @Override
    public void playSpeedFactorChanged(double newPlaySpeedFactor) {
        this.currentPlaybackSpeed = newPlaySpeedFactor;
        if (audioPlayer != null) {
            audioPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
        }
        for (VideoPopup videoPlayer : videoPlayers.values()) {
            videoPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
        }
    }
    private void pausePlaying() {
        if (audioPlayer != null) {
            audioPlayer.pause();
        }

        for (MediaPlayer player : videoPlayers.values()) {
            if (!player.isPaused()) {
                player.pause();
            }
        }
    }

    private void startPlaying() {
        if (audioPlayer != null) {
            audioPlayer.play();
        }
        for (MediaPlayer player : videoPlayers.values()) {
            if (player.isPaused()) {
                player.play();
            }
        }
    }

    @Override
    public void timeChanged(Date raceTime) {
        this.currentRaceTime = raceTime;
        if (audioPlayer != null) {
            audioPlayer.alignTime(this.currentRaceTime);
        }
        for (MediaPlayer player : videoPlayers.values()) {
            player.alignTime(this.currentRaceTime);
        }
    }

    @Override
    public void onFailure(Throwable caught) {
        errorReporter
                .reportError("Remote Procedure Call getPreviousConfigurations() - Failure: " + caught.getMessage());
    }

    @Override
    public void onSuccess(Collection<MediaTrack> mediaTracks) {
        for (MediaTrack mediaTrack : mediaTracks) {
            switch (mediaTrack.type) {
            case VIDEO:
                videoTracks.add(mediaTrack);
            case AUDIO:
                audioTracks.add(mediaTrack);
                break;
            }
        }
        manageMediaButton.setEnabled(mediaTracks.size() > 0);
        toggleMediaButton.setEnabled(mediaTracks.size() > 0);
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void audioChanged(MediaTrack audioTrack) {
        if (audioPlayer != null) {
            
            if (audioPlayer.getMediaTrack() == audioTrack) {
                return; //nothing changed
            }
            
            if (videoPlayers.containsKey(audioPlayer.getMediaTrack())) { //pre-change audioPlayer is one of the videoPlayers
                audioPlayer.setMuted(true);
            } else {
                audioPlayer.pause();
                audioPlayer.setMediaTrack(null);
            }
            audioPlayer = null;
            
        }
        VideoPopup playingVideo = videoPlayers.get(audioTrack);
        if (playingVideo != null) {
            audioPlayer = playingVideo;
            audioPlayer.setMuted(false);
        } else {
            assignNewAudioPlayer(audioTrack);
        }
        
        updateToggleButton();
        
    }

    @Override
    public void videoSelected(MediaTrack videoTrack) {
        VideoPopup playingVideo = videoPlayers.get(videoTrack);
        if (playingVideo == null) {
            final VideoPopup videoPopup = new VideoPopup();
            if ((audioPlayer != null) && (audioPlayer.getMediaTrack() == videoTrack)) { //selected video track has been playing as audio-only
                audioPlayer.pause();
                audioPlayer.setMediaTrack(null);
                audioPlayer = videoPopup;
                videoPopup.setMuted(false);
            } else {
                videoPopup.setMuted(true);
            }
            videoPopup.setMediaTrack(videoTrack);
            synchPlayState(videoPopup);
            videoPopup.show();
            videoPlayers.put(videoTrack, videoPopup);
        } else {
            //nothing changed 
        }
        
        updateToggleButton();
        
    }

    @Override
    public void videoDeselected(MediaTrack videoTrack) {
        VideoPopup removedVideoPlayer = videoPlayers.remove(videoTrack);
        removedVideoPlayer.pause();
        if (removedVideoPlayer == audioPlayer) { //in case this video has been the sound source, replace the video player with a dedicated audio player
            assignNewAudioPlayer(removedVideoPlayer.getMediaTrack());
        }
        removedVideoPlayer.hide();

        updateToggleButton();
        
    }
    
    private void updateToggleButton() {
        toggleMediaButton.setValue((audioPlayer != null) || (!videoPlayers.isEmpty()));
    }

    private void assignNewAudioPlayer(MediaTrack audioTrack) {
        if (audioTrack != null) {
            audioPlayer = new AudioControl();
            audioPlayer.setMediaTrack(audioTrack);
            synchPlayState(audioPlayer);
        } else {
            audioPlayer = null;
        }
    }
    
    private void synchPlayState(final MediaPlayer mediaPlayer) {
        mediaPlayer.setPlaybackSpeed(currentPlaybackSpeed);
        mediaPlayer.alignTime(this.currentRaceTime);
        switch (this.currentPlayState) {
        case Playing:
            mediaPlayer.play();
            break;
        case Paused:
            //fall through to Stopped
        case Stopped:
            mediaPlayer.pause();
        default:
            break;
        }
    }

    public Widget[] widgets() {
        return new Widget[] {toggleMediaButton, manageMediaButton};
    }

}
