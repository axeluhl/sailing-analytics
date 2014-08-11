package com.sap.sailing.gwt.ui.client.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MediaType;
import com.sap.sailing.domain.common.media.MediaTrack.Status;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaSelectionDialog.MediaSelectionListener;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer;
import com.sap.sailing.gwt.ui.client.media.popup.VideoWindowPlayer;
import com.sap.sailing.gwt.ui.client.media.popup.YoutubeWindowPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.MediaPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.player.PlayStateListener;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;

/**
 * The media selector is used as an {@link AsyncCallback} that receives a collection of {@link MediaTrack}s in case the
 * call was successful. This could, e.g., be a call to
 * {@link MediaServiceAsync#getMediaTracksForRace(RegattaAndRaceIdentifier, AsyncCallback)}. When the call returns the
 * track collection, the UI is updated accordingly.
 */
public class MediaSelector implements PlayStateListener, TimeListener,
        AsyncCallback<Collection<MediaTrack>>, MediaSelectionListener, CloseHandler<Window>, ClosingHandler {

    private final CheckBox toggleMediaButton;
    private final Button manageMediaButton;

    private final MediaSelectionDialog mediaSelectionDialog;

    private final Map<MediaTrack, VideoContainer> activeVideoContainers = new HashMap<MediaTrack, VideoContainer>();
    private final Collection<MediaTrack> mediaTracks = new ArrayList<MediaTrack>();

    private final RegattaAndRaceIdentifier raceIdentifier;
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    private Timer raceTimer;
    private final MediaServiceAsync mediaService;
    private StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final UserDTO user;
    private boolean autoSelectMedia;

    private MediaPlayer activeAudioPlayer;
    private Date currentRaceTime;
    private double currentPlaybackSpeed = 1.0d;
    private PlayStates currentPlayState = PlayStates.Paused;

    public MediaSelector(RegattaAndRaceIdentifier selectedRaceIdentifier, RaceTimesInfoProvider raceTimesInfoProvider,
            Timer raceTimer, MediaServiceAsync mediaService, StringMessages stringMessages,
            ErrorReporter errorReporter, UserAgentDetails userAgent, UserDTO user, boolean autoSelectMedia) {
        this.raceIdentifier = selectedRaceIdentifier;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.raceTimer = raceTimer;
        this.mediaService = mediaService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.user = user;
        this.autoSelectMedia = autoSelectMedia;

        Window.addCloseHandler(this);
        Window.addWindowClosingHandler(this);

        mediaSelectionDialog = new MediaSelectionDialog(this);

        manageMediaButton = new Button();
        manageMediaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (mediaSelectionDialog.isShowing()) {
                    hideSelectionDialog();
                } else {
                    showSelectionDialog();
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

    }

    private boolean isPotentiallyPlayable(MediaTrack mediaTrack) {
        return MediaTrack.Status.REACHABLE.equals(mediaTrack.status)
                || MediaTrack.Status.UNDEFINED.equals(mediaTrack.status);
    }

    private void setStatus(final MediaTrack mediaTrack) {
        if (!mediaTrack.isYoutube()) {
            // firefox crashes in the current version when trying to read the metadata from mp4 files
            if(!userAgent.getType().equals(AgentTypes.FIREFOX)) {
                Audio audio = Audio.createIfSupported();
                if (audio != null) {
                    AudioElement mediaReachableTester = audio.getAudioElement();
                    addLoadMetadataHandler(mediaReachableTester, mediaTrack);
                    mediaReachableTester.setPreload(MediaElement.PRELOAD_METADATA);
                    mediaReachableTester.setSrc(mediaTrack.url);
                    mediaReachableTester.load();
                } else {
                    mediaTrack.status = Status.CANNOT_PLAY;
                }
            } else {
                mediaTrack.status = Status.REACHABLE;
            }
        } else {
            mediaTrack.status = Status.REACHABLE;
        }
    }

    native void addLoadMetadataHandler(MediaElement mediaElement, MediaTrack mediaTrack) /*-{
		var that = this;
		mediaElement
				.addEventListener(
						'loadedmetadata',
						function() {
							that.@com.sap.sailing.gwt.ui.client.media.MediaSelector::loadedmetadata(Lcom/sap/sailing/domain/common/media/MediaTrack;)(mediaTrack);
						});
		mediaElement
				.addEventListener(
						'error',
						function() {
							that.@com.sap.sailing.gwt.ui.client.media.MediaSelector::mediaError(Lcom/sap/sailing/domain/common/media/MediaTrack;)(mediaTrack);
						});
    }-*/;

    public void loadedmetadata(MediaTrack mediaTrack) {
        mediaTrack.status = Status.REACHABLE;
    }

    public void mediaError(MediaTrack mediaTrack) {
        mediaTrack.status = Status.NOT_REACHABLE;
    }

    private void playDefault() {
        MediaTrack defaultVideo = getDefaultVideo();
        if (defaultVideo != null) {
            videoSelected(defaultVideo);
        }
        MediaTrack defaultAudio = getDefaultAudio();
        if (defaultAudio != null) {
            audioChanged(defaultAudio);
        }
    }

    private MediaTrack getDefaultAudio() {
        // TODO: implement a better heuristic than just taking the first to come
        for (MediaTrack mediaTrack : mediaTracks) {
            if (MediaType.audio.equals(mediaTrack.mimeType.mediaType) && isPotentiallyPlayable(mediaTrack)) {
                return mediaTrack;
            }
        }
        return getDefaultVideo();
    }

    private MediaTrack getDefaultVideo() {
        for (MediaTrack mediaTrack : mediaTracks) {
            if (MediaType.video.equals(mediaTrack.mimeType.mediaType) && isPotentiallyPlayable(mediaTrack)) {
                return mediaTrack;
            }
        }
        return null;
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
        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
            videoPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
        }
    }

    private void pausePlaying() {
        if (activeAudioPlayer != null) {
            activeAudioPlayer.pauseMedia();
        }

        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
            if (!videoPlayer.isMediaPaused()) {
                videoPlayer.pauseMedia();
            }
        }
    }

    private void startPlaying() {
        if ((activeAudioPlayer != null) && activeAudioPlayer.isCoveringCurrentRaceTime()) {
            activeAudioPlayer.playMedia();
        }
        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
            if (videoPlayer.isMediaPaused() && videoPlayer.isCoveringCurrentRaceTime()) {
                videoPlayer.playMedia();
            }
        }
    }

    @Override
    public void timeChanged(Date newRaceTime, Date oldRaceTime) {
        this.currentRaceTime = newRaceTime;
        if (activeAudioPlayer != null) {
            activeAudioPlayer.raceTimeChanged(this.currentRaceTime);
            ensurePlayState(activeAudioPlayer);
        }
        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
            videoPlayer.raceTimeChanged(this.currentRaceTime);
            ensurePlayState(videoPlayer);
        }
    }

    @Override
    public void onFailure(Throwable caught) {
        setWidgetsVisible((this.user != null));
        errorReporter.reportError("Remote Procedure Call getMediaTracksForRace(...) - Failure: " + caught.getMessage());
    }

    @Override
    public void onSuccess(Collection<MediaTrack> mediaTracks) {
        this.mediaTracks.clear();
        this.mediaTracks.addAll(mediaTracks);
        for (MediaTrack mediaTrack : this.mediaTracks) {
            setStatus(mediaTrack);
        }
        setWidgetsVisible((this.mediaTracks.size() > 0) || (this.user != null));
        
        toggleMediaButton.setValue(autoSelectMedia);
        if (autoSelectMedia) {
            playDefault();
        }
    }

    private void setWidgetsVisible(boolean isVisible) {
        manageMediaButton.setVisible(isVisible);
        toggleMediaButton.setVisible(isVisible);
    }

    // @Override
    public void _raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void audioChanged(MediaTrack audioTrack) {
        if (activeAudioPlayer != null) { // --> then reset active audio player

            if (activeAudioPlayer.getMediaTrack() == audioTrack) {
                return; // nothing changed
            }

            if (activeVideoContainers.containsKey(activeAudioPlayer.getMediaTrack())) { // pre-change audioPlayer is one of the
                                                                               // videoPlayers
                activeAudioPlayer.setMuted(true);
            } else { // pre-change audioPlayer is a dedicated audio-only player
                activeAudioPlayer.shutDown();
            }
            activeAudioPlayer = null;
        }
        if ((audioTrack != null) && audioTrack.isYoutube()) { // --> Youtube videos can't be played for audio-only. So
                                                              // add a video player first.
            videoSelected(audioTrack);
            mediaSelectionDialog.selectVideo(audioTrack);
        }
        VideoContainer playingVideoContainer = activeVideoContainers.get(audioTrack);
        if (playingVideoContainer != null) {
            VideoPlayer playingVideoPlayer = playingVideoContainer.getVideoPlayer();
            activeAudioPlayer = playingVideoPlayer;
            activeAudioPlayer.setMuted(false);
        } else {
            assignNewAudioPlayer(audioTrack);
        }

        updateToggleButton();

    }

    @Override
    public void videoSelected(final MediaTrack videoTrack) {
        VideoContainer activeVideoContainer = activeVideoContainers.get(videoTrack);
        if (activeVideoContainer == null) {
            final PopoutWindowPlayer.PopoutCloseListener popupCloseListener = new PopoutWindowPlayer.PopoutCloseListener() {
                
                private VideoContainer videoContainer;
                
                @Override
                public void popoutClosed() {
                    if (videoContainer == null) {
                        videoDeselected(videoTrack);
                    } else {
                        mediaSelectionDialog.hide();
                        registerVideoContainer(videoTrack, videoContainer);
                        videoContainer = null;
                    }
                }

                @Override
                public void setVideoContainer(VideoContainer videoContainer) {
                    this.videoContainer = videoContainer; 
                }
            };
            PopoutListener popoutListener = new PopoutListener() {

                @Override
                public void popoutVideo(MediaTrack videoTrack) {
                    VideoContainer videoContainer;
                    if (videoTrack.isYoutube()) {
                        videoContainer = new YoutubeWindowPlayer(videoTrack, popupCloseListener);
                    } else {
                        videoContainer = new VideoWindowPlayer(videoTrack, popupCloseListener);
                    }
                    popupCloseListener.setVideoContainer(videoContainer);
                    videoDeselected(videoTrack);
                }
            };

            final VideoSynchPlayer videoPlayer;
            boolean showSynchControls = this.user != null;
            
            if (videoTrack.isYoutube()) {
                // popupPlayer = new YoutubeWindowPlayer(videoTrack, popCloseListener);
                videoPlayer = new VideoYoutubePlayer(videoTrack, getRaceStartTime(), showSynchControls, raceTimer, errorReporter, popupCloseListener, popoutListener);
            } else {
                // popupPlayer = new VideoWindowPlayer(videoTrack, popCloseListener);
                videoPlayer = new VideoHtmlPlayer(videoTrack, getRaceStartTime(), showSynchControls, raceTimer, errorReporter, popupCloseListener, popoutListener);
            }
            VideoFloatingContainer videoFloatingContainer = new VideoFloatingContainer(videoPlayer, videoTrack, showSynchControls, mediaService, errorReporter, popupCloseListener, popoutListener);
            
            registerVideoContainer(videoTrack, videoFloatingContainer);
        } else {
            // nothing changed
        }

    }

    private void registerVideoContainer(final MediaTrack videoTrack, final VideoContainer videoContainer) {
        VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
        activeVideoContainers.put(videoTrack, videoContainer);
        if ((activeAudioPlayer != null) && (activeAudioPlayer.getMediaTrack() == videoTrack)) { // selected video track
                                                                                                // has been playing as
                                                                                                // audio-only
            activeAudioPlayer.pauseMedia();
            activeAudioPlayer = videoContainer.getVideoPlayer();
            videoPlayer.setMuted(false);
        } else {
            videoPlayer.setMuted(true);
        }
        synchPlayState(videoPlayer);
        updateToggleButton();
    }

    private long getRaceStartTime() {
        return raceTimesInfoProvider.getRaceTimesInfo(raceIdentifier).startOfRace.getTime();
    }

    @Override
    public void videoDeselected(MediaTrack videoTrack) {
        mediaSelectionDialog.unselectVideo(videoTrack);
        VideoContainer removedVideoContainer = activeVideoContainers.remove(videoTrack);
        if (removedVideoContainer != null) {
            removedVideoContainer.shutDown();
            VideoPlayer removedVideoPlayer = removedVideoContainer.getVideoPlayer(); 
            if (removedVideoPlayer == activeAudioPlayer) { // in case this video has been the sound source, replace the
                                                           // video player with a dedicated audio player
                if (removedVideoPlayer.getMediaTrack().isYoutube()) {
                    assignNewAudioPlayer(null);
                } else {
                    assignNewAudioPlayer(removedVideoPlayer.getMediaTrack());
                }
            }
        }

        updateToggleButton();

    }

    private void updateToggleButton() {
        toggleMediaButton.setValue((activeAudioPlayer != null) || (!activeVideoContainers.isEmpty()));
    }

    private void assignNewAudioPlayer(MediaTrack audioTrack) {
        if (audioTrack != null) {
            activeAudioPlayer = new AudioHtmlPlayer(audioTrack);

            synchPlayState(activeAudioPlayer);
        } else {
            activeAudioPlayer = null;
        }
    }

    private void synchPlayState(final MediaPlayer mediaPlayer) {
        mediaPlayer.setPlaybackSpeed(currentPlaybackSpeed);
        mediaPlayer.raceTimeChanged(this.currentRaceTime);
        ensurePlayState(mediaPlayer);
    }

    private void ensurePlayState(final MediaPlayer mediaPlayer) {
        switch (this.currentPlayState) {
        case Playing:
            if (mediaPlayer.isMediaPaused() && mediaPlayer.isCoveringCurrentRaceTime()) {
                mediaPlayer.playMedia();
            }
            break;
        case Paused:
            if (!mediaPlayer.isMediaPaused()) {
                mediaPlayer.pauseMedia();
            }
        default:
            break;
        }
    }

    public Widget[] widgets() {
        return new Widget[] { toggleMediaButton, manageMediaButton};
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
            activeVideoContainers.remove(activeAudioPlayer.getMediaTrack()); // just to ensure that a potentially audio-playing video player is
                                                    // not destroyed a second time in the following video loop.
            activeAudioPlayer.shutDown();
            activeAudioPlayer = null;
        }
        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            videoContainer.shutDown();
        }
        activeVideoContainers.clear();
        updateToggleButton();
    }

    @Override
    public void addMediaTrack() {
        hideSelectionDialog();
        Date defaultStartTime = new Date(getRaceStartTime());
        NewMediaDialog dialog = new NewMediaDialog(defaultStartTime, MediaSelector.this.stringMessages,
                new DialogCallback<MediaTrack>() {

                    @Override
                    public void cancel() {
                        // no op
                    }

                    @Override
                    public void ok(final MediaTrack mediaTrack) {
                        MediaSelector.this.mediaService.addMediaTrack(mediaTrack, new AsyncCallback<String>() {

                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError(t.toString());
                            }

                            @Override
                            public void onSuccess(String dbId) {
                                mediaTrack.dbId = dbId;
                                mediaTracks.add(mediaTrack);
                                videoSelected(mediaTrack);
                            }
                        });

                    }
                });
        dialog.show();
    }

    @Override
    public void deleteMediaTrack(final MediaTrack mediaTrack) {
        if (Window.confirm(stringMessages.reallyRemoveMediaTrack(mediaTrack.title))) {
            mediaSelectionDialog.hide();
            mediaService.deleteMediaTrack(mediaTrack, new AsyncCallback<Void>() {
                
                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError(t.toString());
                }

                @Override
                public void onSuccess(Void _void) {
                    MediaSelector.this.videoDeselected(mediaTrack);
                    mediaTracks.remove(mediaTrack);
                }
            });
        }
    }

    @Override
    public boolean allowsAddDelete() {
        return this.user != null;
    }

    private void showSelectionDialog() {
        MediaTrack playingAudioTrack = activeAudioPlayer != null ? activeAudioPlayer.getMediaTrack() : null;
        Set<MediaTrack> playingVideoTracks = activeVideoContainers.keySet();

        Collection<MediaTrack> reachableVideoTracks = new ArrayList<MediaTrack>();
        Collection<MediaTrack> reachableAudioTracks = new ArrayList<MediaTrack>();
        for (MediaTrack mediaTrack : mediaTracks) {
            if (isPotentiallyPlayable(mediaTrack)) {
                switch (mediaTrack.mimeType.mediaType) {
                case video:
                    reachableVideoTracks.add(mediaTrack);
                case audio: // intentional fall through
                    if(userAgent.getType().equals(AgentTypes.FIREFOX)) {
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

        boolean showAddButton = MediaSelector.this.user != null;
        mediaSelectionDialog.show(reachableVideoTracks, playingVideoTracks, reachableAudioTracks,
                playingAudioTrack, showAddButton, toggleMediaButton);
    }

    private void hideSelectionDialog() {
        mediaSelectionDialog.hide();
    }

}
