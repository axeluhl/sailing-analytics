package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
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
import com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer.PopupCloseListener;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class MediaSelector implements RaceTimesInfoProviderListener, PlayStateListener, TimeListener,
        AsyncCallback<Collection<MediaTrack>>, MediaSelectionListener, CloseHandler<Window>, ClosingHandler {

    private final CheckBox toggleMediaButton;
    private final Button manageMediaButton;
    
    private final MediaSelectionDialog mediaSelectionDialog;
    private final AudioElement mediaCanPlayTester; 

    private MediaPlayer activeAudioPlayer;
    private final Map<MediaTrack, MediaPlayer> videoPlayers = new HashMap<MediaTrack, MediaPlayer>();
    private final Set<MediaTrack> audioTracks = new HashSet<MediaTrack>();
    private final Set<MediaTrack> videoTracks = new HashSet<MediaTrack>();
    private final ErrorReporter errorReporter;

    private Date currentRaceTime;
    private double currentPlaybackSpeed = 1.0d;
    private PlayStates currentPlayState = PlayStates.Paused;

    public MediaSelector(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        
        Window.addCloseHandler(this);
        Window.addWindowClosingHandler(this);
        
        mediaSelectionDialog = new MediaSelectionDialog(this);         

        manageMediaButton = new Button();
        manageMediaButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (mediaSelectionDialog.isShowing()) {
                    mediaSelectionDialog.hide();
                } else {
                    MediaTrack playingAudioTrack = activeAudioPlayer != null ? activeAudioPlayer.getMediaTrack() : null;
                    Set<MediaTrack> playingVideoTracks = videoPlayers.keySet();
                    mediaSelectionDialog.show(videoTracks, playingVideoTracks, audioTracks, playingAudioTrack, toggleMediaButton);
                }
            }
        });
        manageMediaButton.addStyleName("raceBoardNavigation-settingsButton");
        manageMediaButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        manageMediaButton.setTitle("Configure Media");


        toggleMediaButton = new CheckBox("Audio & Video");
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
        
        setWidgetsVisible(false);

        Audio audio = Audio.createIfSupported();
        if (audio != null) {            
            mediaCanPlayTester = audio.getAudioElement();
        } else {
            mediaCanPlayTester = null;
        }
    }
    
    private boolean canPlay(MediaTrack mediaTrack) {
        if (mediaCanPlayTester != null) {
            if (mediaTrack.isYoutube()) {
                return true;
            } else {
                String canPlay = mediaCanPlayTester.canPlayType(mediaTrack.typeToString());
                if (mediaCanPlayTester != null) {
                    return MediaElement.CAN_PLAY_PROBABLY.equals(canPlay) || MediaElement.CAN_PLAY_MAYBE.equals(canPlay);
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
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
            return getDefaultVideo();
        }
    }

    private MediaTrack getDefaultVideo() {
        if (!videoTracks.isEmpty()) {
            return videoTracks.iterator().next();
        } else {
            return null;
        }
    }

    private void stopAll() {
        clear();
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
        if (activeAudioPlayer != null) {
            activeAudioPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
        }
        for (MediaPlayer videoPlayer : videoPlayers.values()) {
            videoPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
        }
    }
    
    private void pausePlaying() {
        if (activeAudioPlayer != null) {
            activeAudioPlayer.pause();
        }

        for (MediaPlayer player : videoPlayers.values()) {
            if (!player.isPaused()) {
                player.pause();
            }
        }
    }

    private void startPlaying() {
        if (activeAudioPlayer != null) {
            activeAudioPlayer.play();
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
        if (activeAudioPlayer != null) {
            activeAudioPlayer.alignTime(this.currentRaceTime);
        }
        for (MediaPlayer player : videoPlayers.values()) {
            player.alignTime(this.currentRaceTime);
        }
    }

    @Override
    public void onFailure(Throwable caught) {
        setWidgetsVisible(false);
        errorReporter
                .reportError("Remote Procedure Call getMediaTracksForRace(...) - Failure: " + caught.getMessage());
    }

    @Override
    public void onSuccess(Collection<MediaTrack> mediaTracks) {
        for (MediaTrack mediaTrack : mediaTracks) {
            if (canPlay(mediaTrack)) {
                switch (mediaTrack.mimeType.mediaType) {
                case video:
                    videoTracks.add(mediaTrack);
                case audio: //Intentional fall through. Video tracks are also considered audio tracks!
                    audioTracks.add(mediaTrack); 
                    break;
                }
            }
        }
        setWidgetsVisible(mediaTracks.size() > 0);
    }

    private void setWidgetsVisible(boolean isVisible) {
        manageMediaButton.setVisible(isVisible);
        toggleMediaButton.setVisible(isVisible);
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void audioChanged(MediaTrack audioTrack) {
        if (activeAudioPlayer != null) { // --> then reset active audio player 
            
            if (activeAudioPlayer.getMediaTrack() == audioTrack) {
                return; //nothing changed
            }
            
            if (videoPlayers.containsKey(activeAudioPlayer.getMediaTrack())) { //pre-change audioPlayer is one of the videoPlayers
                activeAudioPlayer.setMuted(true);
            } else { //pre-change audioPlayer is a dedicated audio-only player
                activeAudioPlayer.destroy();
            }
            activeAudioPlayer = null;
        }
        if ((audioTrack != null) && audioTrack.isYoutube()) { // --> Youtube videos can't be played for audio-only. So add a video player first.
            videoSelected(audioTrack);
            mediaSelectionDialog.selectVideo(audioTrack);
        }
        MediaPlayer playingVideo = videoPlayers.get(audioTrack);
        if (playingVideo != null) {
            activeAudioPlayer = playingVideo;
            activeAudioPlayer.setMuted(false);
        } else {
            assignNewAudioPlayer(audioTrack);
        }
        
        updateToggleButton();
        
    }

    @Override
    public void videoSelected(final MediaTrack videoTrack) {
        MediaPlayer playingVideo = videoPlayers.get(videoTrack);
        if (playingVideo == null) {
            PopupCloseListener popCloseListener = new PopupCloseListener() {
                @Override
                public void popupClosed() {
                    videoDeselected(videoTrack);
                }
            };
            final PopupWindowPlayer popupPlayer;
            if (videoTrack.isYoutube()) {
                popupPlayer = new YoutubeWindowPlayer(videoTrack, popCloseListener);
            } else {
                popupPlayer = new VideoWindowPlayer(videoTrack, popCloseListener);
            }
            videoPlayers.put(videoTrack, popupPlayer);
            if ((activeAudioPlayer != null) && (activeAudioPlayer.getMediaTrack() == videoTrack)) { //selected video track has been playing as audio-only
                activeAudioPlayer.pause();
                activeAudioPlayer = popupPlayer;
                popupPlayer.setMuted(false);
            } else {
                popupPlayer.setMuted(true);
            }
            synchPlayState(popupPlayer);
        } else {
            //nothing changed 
        }
        
        updateToggleButton();
        
    }

    @Override
    public void videoDeselected(MediaTrack videoTrack) {
        mediaSelectionDialog.unselectVideo(videoTrack);
        MediaPlayer removedVideoPlayer = videoPlayers.remove(videoTrack);
        removedVideoPlayer.destroy();
        if (removedVideoPlayer == activeAudioPlayer) { //in case this video has been the sound source, replace the video player with a dedicated audio player
            if (removedVideoPlayer.getMediaTrack().isYoutube()) {
                assignNewAudioPlayer(null);
            } else {
                assignNewAudioPlayer(removedVideoPlayer.getMediaTrack());
            }
        }

        updateToggleButton();
        
    }
    
    private void updateToggleButton() {
        toggleMediaButton.setValue((activeAudioPlayer != null) || (!videoPlayers.isEmpty()));
    }

    private void assignNewAudioPlayer(MediaTrack audioTrack) {
        if (audioTrack != null) {
            activeAudioPlayer = new AudioPlayer(audioTrack);
            
            synchPlayState(activeAudioPlayer);
        } else {
            activeAudioPlayer = null;
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

    @Override
    public void onClose(CloseEvent<Window> arg0) {
        clear();
    }

    @Override
    public void onWindowClosing(ClosingEvent arg0) {
        clear();
    }

    private void clear() {
        if (activeAudioPlayer != null) {
            videoPlayers.remove(activeAudioPlayer); //just to ensure that a potentially audio-playing video player is not destroyed a second time in the following video loop. 
            activeAudioPlayer.destroy();
            activeAudioPlayer = null;
        }
        for (MediaPlayer videoControl : videoPlayers.values()) {
            videoControl.destroy();
        }
        videoPlayers.clear();
        updateToggleButton();
    }

}
