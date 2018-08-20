package com.sap.sailing.gwt.ui.client.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.Status;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.SailingPermissionsForRoleProvider;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.popup.VideoJSWindowPlayer;
import com.sap.sailing.gwt.ui.client.media.popup.YoutubeWindowPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.MediaPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MediaType;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.player.PlayStateListener;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserDTO;

public class MediaPlayerManagerComponent extends AbstractComponent<MediaPlayerSettings> implements PlayStateListener, TimeListener,
        MediaPlayerManager, CloseHandler<Window>, ClosingHandler {

    static interface VideoContainerFactory<T> {
        T createVideoContainer(VideoSynchPlayer videoPlayer, UserService userService, MediaServiceAsync mediaService,
                ErrorReporter errorReporter, PlayerCloseListener playerCloseListener, PopoutListener popoutListener);
    }
    
    private final SimplePanel rootPanel = new SimplePanel();
    private final UserService userService;

    private MediaPlayer activeAudioPlayer;
    private VideoPlayer dockedVideoPlayer;
    private final Map<MediaTrack, VideoContainer> activeVideoContainers = new HashMap<MediaTrack, VideoContainer>();
    private Collection<MediaTrack> assignedMediaTracks = new ArrayList<>();
    private Collection<MediaTrack> overlappingMediaTracks = new ArrayList<>();

    private final RegattaAndRaceIdentifier raceIdentifier;
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    private final Timer raceTimer;
    private final MediaServiceAsync mediaService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final PopupPositionProvider popupPositionProvider;
    private MediaPlayerSettings settings;
    private final MediaPlayerLifecycle mediaPlayerLifecycle;

    private List<PlayerChangeListener> playerChangeListener = new ArrayList<>();

    public MediaPlayerManagerComponent(Component<?> parent, ComponentContext<?> context,
            MediaPlayerLifecycle mediaPlayerLifecycle,
            RegattaAndRaceIdentifier selectedRaceIdentifier,
            RaceTimesInfoProvider raceTimesInfoProvider, Timer raceTimer, MediaServiceAsync mediaService,
            UserService userService, StringMessages stringMessages, ErrorReporter errorReporter,
            UserAgentDetails userAgent, PopupPositionProvider popupPositionProvider, MediaPlayerSettings settings) {
        super(parent, context);
        this.mediaPlayerLifecycle = mediaPlayerLifecycle;
        this.userService = userService;
        this.raceIdentifier = selectedRaceIdentifier;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.raceTimer = raceTimer;
        this.raceTimer.addPlayStateListener(this);
        this.raceTimer.addTimeListener(this);
        this.playSpeedFactorChanged(raceTimer.getPlaySpeedFactor());
        this.timeChanged(raceTimer.getTime(), null);
        this.playStateChanged(raceTimer.getPlayState(), raceTimer.getPlayMode());
        this.mediaService = mediaService;
        mediaService.getMediaTracksForRace(this.getCurrentRace(), getAssignedMediaCallback());
        mediaService.getMediaTracksInTimeRange(this.getCurrentRace(), getOverlappingMediaCallback());
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.popupPositionProvider = popupPositionProvider;
        this.settings = settings;
        Window.addCloseHandler(this);
        Window.addWindowClosingHandler(this);
    }

    private static boolean isPotentiallyPlayable(MediaTrack mediaTrack) {
        return MediaTrack.Status.REACHABLE.equals(mediaTrack.status)
                || MediaTrack.Status.UNDEFINED.equals(mediaTrack.status);
    }

    private void setStatus(final MediaTrack mediaTrack) {
        if (!mediaTrack.isYoutube()) {
            // firefox crashes in the current version when trying to read the metadata from mp4 files
            if (!userAgent.getType().equals(AgentTypes.FIREFOX)) {
                Audio audio = Audio.createIfSupported();
                if (audio != null) {
                    AudioElement mediaReachableTester = audio.getAudioElement();
                    addLoadMetadataHandler(mediaReachableTester, mediaTrack);
                    mediaReachableTester.setPreload(MediaElement.PRELOAD_METADATA);
                    mediaReachableTester.setSrc(UriUtils.fromString(mediaTrack.url).asString());
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
							that.@com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent::loadedmetadata(Lcom/sap/sailing/domain/common/media/MediaTrack;)(mediaTrack);
						});
		mediaElement
				.addEventListener(
						'error',
						function() {
							that.@com.sap.sailing.gwt.ui.client.media.MediaPlayerManagerComponent::mediaError(Lcom/sap/sailing/domain/common/media/MediaTrack;)(mediaTrack);
						});
    }-*/;

    public void loadedmetadata(MediaTrack mediaTrack) {
        mediaTrack.status = Status.REACHABLE;
    }

    public void mediaError(MediaTrack mediaTrack) {
        mediaTrack.status = Status.NOT_REACHABLE;
    }

    @Override
    public void playDefault() {
        MediaTrack defaultVideo = getDefaultVideo();
        if (defaultVideo != null) {
            playFloatingVideo(defaultVideo);
            playAudio(defaultVideo);
        } else {
            MediaTrack defaultAudio = getDefaultAudio();
            if (defaultAudio != null) {
                playAudio(defaultAudio);
            }
        }
    }

    private MediaTrack getDefaultAudio() {
        // TODO: implement a better heuristic than just taking the first to come
        for (MediaTrack mediaTrack : assignedMediaTracks) {
            if (mediaTrack.mimeType != null && MediaType.audio.equals(mediaTrack.mimeType.mediaType) && isPotentiallyPlayable(mediaTrack)) {
                return mediaTrack;
            }
        }
        return null;
    }

    private MediaTrack getDefaultVideo() {
        for (MediaTrack mediaTrack : assignedMediaTracks) {
            if (mediaTrack.mimeType != null && MediaType.video.equals(mediaTrack.mimeType.mediaType)
                    && isPotentiallyPlayable(mediaTrack)) {
                return mediaTrack;
            }
        }
        return null;
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        switch (playMode) {
        case Replay:
            switch (this.raceTimer.getPlayState()) {
            case Playing:
                startPlaying();
                break;
            case Paused:
                pausePlaying();
            default:
                break;
            }
            break;
        case Live:
            // TODO: Live mode not supported, yet.
            startPlaying();
            break;
        default:
            break;
        }
    }

    @Override
    public void playSpeedFactorChanged(double newPlaySpeedFactor) {
        if (isStandaloneAudio()) {// only if audio player isn't one of the video players anyway
            activeAudioPlayer.setPlaybackSpeed(newPlaySpeedFactor);
        }
        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
            videoPlayer.setPlaybackSpeed(newPlaySpeedFactor);
        }
    }

    /**
     * Checks if audio player isn't one of the video players
     * 
     * @return
     */
    private boolean isStandaloneAudio() {
        return activeAudioPlayer != null && !activeVideoContainers.containsKey(activeAudioPlayer.getMediaTrack());
    }

    private void pausePlaying() {
        if (isStandaloneAudio()) { // only if audio player isn't one of the video players anyway
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
        if (isStandaloneAudio() && activeAudioPlayer.isCoveringCurrentRaceTime()) {
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
        if (isStandaloneAudio()) { // only if audio player isn't one of the video players anyway
            ensurePlayState(activeAudioPlayer);
            activeAudioPlayer.raceTimeChanged(newRaceTime);
        }
        for (VideoContainer videoContainer : activeVideoContainers.values()) {
            VideoPlayer videoPlayer = videoContainer.getVideoPlayer();
            ensurePlayState(videoPlayer);
            videoPlayer.raceTimeChanged(newRaceTime);
        }
    }

    /**
     * Wraps the callback handling functions in an object to better document their purpose. onSuccess and onError are
     * simply too generic to tell about their concrete use.
     * 
     * @return
     */
    private AsyncCallback<Iterable<MediaTrack>> getAssignedMediaCallback() {
        return new AsyncCallback<Iterable<MediaTrack>>() {
            @Override
            public void onFailure(Throwable caught) {
                notifyStateChange();
                errorReporter.reportError(stringMessages.remoteProcedureCall()+ "getMediaTracksForRace(...) - " +stringMessages.error()
                + caught.getMessage());
                
            }

            @Override
            public void onSuccess(Iterable<MediaTrack> mediaTracks) {
                MediaPlayerManagerComponent.this.assignedMediaTracks.clear();
                Util.addAll(mediaTracks, MediaPlayerManagerComponent.this.assignedMediaTracks);
                for (MediaTrack mediaTrack : MediaPlayerManagerComponent.this.assignedMediaTracks) {
                    setStatus(mediaTrack);
                }
                if (settings.isAutoSelectMedia()) {
                    playDefault();
                }
                notifyStateChange();
            }
        };
    }

    /**
     * Wraps the callback handling functions in an object to better document their purpose. onSuccess and onError are
     * simply too generic to tell about their concrete use.
     * 
     * @return
     */
    private AsyncCallback<Iterable<MediaTrack>> getOverlappingMediaCallback() {
        return new AsyncCallback<Iterable<MediaTrack>>() {
            @Override
            public void onFailure(Throwable caught) {
                notifyStateChange();
                errorReporter.reportError(stringMessages.remoteProcedureCall()+ "getMediaTracksForRace(...) - " +stringMessages.error()
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(Iterable<MediaTrack> mediaTracks) {
                MediaPlayerManagerComponent.this.overlappingMediaTracks.clear();
                Util.addAll(mediaTracks, MediaPlayerManagerComponent.this.overlappingMediaTracks);
                for (MediaTrack mediaTrack : MediaPlayerManagerComponent.this.overlappingMediaTracks) {
                    setStatus(mediaTrack);
                }
                notifyStateChange();
            }
        };
    }

    private void notifyStateChange() {
        for(PlayerChangeListener listener:playerChangeListener) {
            listener.notifyStateChange();
            
        }
    }

    @Override
    public void playDockedVideo(MediaTrack videoTrack) {
        if ((dockedVideoPlayer == null) || (dockedVideoPlayer.getMediaTrack() != videoTrack)) {
            closeDockedVideo();
            closeFloatingVideo(videoTrack);
            VideoContainer videoDockedContainer = createAndWrapVideoPlayer(videoTrack,
                    new VideoContainerFactory<VideoDockedContainer>() {
                        @Override
                        public VideoDockedContainer createVideoContainer(VideoSynchPlayer videoPlayer,
                                UserService userService, MediaServiceAsync mediaService, ErrorReporter errorReporter,
                                PlayerCloseListener playerCloseListener, PopoutListener popoutListener) {
                            VideoDockedContainer videoDockedContainer = new VideoDockedContainer(rootPanel,
                                    videoPlayer, playerCloseListener, popoutListener);
                            return videoDockedContainer;
                        }
                    });
            registerVideoContainer(videoTrack, videoDockedContainer);
            notifyStateChange();
        } else {
            // nothing changed
        }
    }

    @Override
    public void closeDockedVideo() {
        if (dockedVideoPlayer != null) {
            dockedVideoPlayer.shutDown();
            dockedVideoPlayer = null;
            notifyStateChange();
        } else {
            // nothing changed
        }
    }

    @Override
    public void playAudio(MediaTrack audioTrack) {
        if ((activeAudioPlayer == null) || (activeAudioPlayer.getMediaTrack() != audioTrack)) {
            muteAudio();
            if ((audioTrack != null) && audioTrack.isYoutube()) { // --> Youtube videos can't be played for audio-only.
                                                                  // So add a video player first.
                playFloatingVideo(audioTrack);
            }
            VideoContainer playingVideoContainer = activeVideoContainers.get(audioTrack);
            if (playingVideoContainer != null) {
                VideoPlayer playingVideoPlayer = playingVideoContainer.getVideoPlayer();
                activeAudioPlayer = playingVideoPlayer;
                activeAudioPlayer.setMuted(false);
            } else {
                assignNewAudioPlayer(audioTrack);
            }
            notifyStateChange();
        } else {
            // nothing changed
        }
    }

    @Override
    public void muteAudio() {
        if (activeAudioPlayer != null) { // --> then reset active audio player

            if (activeVideoContainers.containsKey(activeAudioPlayer.getMediaTrack())) { // pre-change audioPlayer is one
                                                                                        // of the
                // videoPlayers
                activeAudioPlayer.setMuted(true);
            } else { // pre-change audioPlayer is a dedicated audio-only player
                activeAudioPlayer.shutDown();
            }
            activeAudioPlayer = null;
            notifyStateChange();
        } else {
            // nothing changed
        }
    }

    @Override
    public void playFloatingVideo(final MediaTrack videoTrack) {
        if (dockedVideoPlayer != null && dockedVideoPlayer.getMediaTrack() == videoTrack) {
            closeDockedVideo();
        }
        VideoContainer activeVideoContainer = activeVideoContainers.get(videoTrack);
        if (activeVideoContainer == null) {
            VideoFloatingContainer videoFloatingContainer = createAndWrapVideoPlayer(videoTrack,
                    new VideoContainerFactory<VideoFloatingContainer>() {
                        @Override
                        public VideoFloatingContainer createVideoContainer(VideoSynchPlayer videoPlayer,
                                UserService userservice, MediaServiceAsync mediaService, ErrorReporter errorReporter,
                                PlayerCloseListener playerCloseListener, PopoutListener popoutListener) {
                            VideoFloatingContainer videoFloatingContainer = new VideoFloatingContainer(videoPlayer, popupPositionProvider,
                                    userservice, mediaService, errorReporter, playerCloseListener, popoutListener);
                            return videoFloatingContainer;
                        }
                    });

            registerVideoContainer(videoTrack, videoFloatingContainer);
            notifyStateChange();
        } else {
            // nothing changed
        }
    }

    private <T> T createAndWrapVideoPlayer(final MediaTrack videoTrack, VideoContainerFactory<T> videoContainerFactory) {
        final PopoutWindowPlayer.PlayerCloseListener playerCloseListener = new PopoutWindowPlayer.PlayerCloseListener() {
            private VideoContainer videoContainer;

            @Override
            public void playerClosed() {
                if (videoContainer == null) {
                    closeFloatingVideo(videoTrack);
                } else {
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
                    videoContainer = new YoutubeWindowPlayer(videoTrack, playerCloseListener);
                } else {
                    videoContainer = new VideoJSWindowPlayer(videoTrack, playerCloseListener);
                }
                playerCloseListener.setVideoContainer(videoContainer);
                closeFloatingVideo(videoTrack);
            }
        };
        final VideoSynchPlayer videoPlayer;
        if (videoTrack.isYoutube()) {
            videoPlayer = new VideoYoutubePlayer(videoTrack, getRaceStartTime(), raceTimer);
        } else {
            videoPlayer = new VideoJSSyncPlayer(videoTrack, getRaceStartTime(), raceTimer);
        }
        return videoContainerFactory.createVideoContainer(videoPlayer, userService, getMediaService(), errorReporter,
                playerCloseListener, popoutListener);
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
        notifyStateChange();
    }

    private TimePoint getRaceStartTime() {
        Date startOfRace = raceTimesInfoProvider.getRaceTimesInfo(getCurrentRace()).startOfRace;
        if (startOfRace != null) {
            return new MillisecondsTimePoint(startOfRace);
        } else {
            return null;
        }
    }
    
    private TimePoint getTrackingStartTime() {
        Date startOfTracking = raceTimesInfoProvider.getRaceTimesInfo(getCurrentRace()).startOfTracking;
        if (startOfTracking != null) {
            return new MillisecondsTimePoint(startOfTracking);
        } else {
            return null;
        }
    }

    @Override
    public void closeFloatingVideo(MediaTrack videoTrack) {
        VideoContainer removedVideoContainer = activeVideoContainers.remove(videoTrack);
        if (removedVideoContainer != null) {
            removedVideoContainer.shutDown();
            if (activeAudioPlayer != null && activeAudioPlayer.getMediaTrack() == videoTrack) {
                assignNewAudioPlayer(null);
            }
            notifyStateChange();
        } else {
            // nothing changed
        }
    }

    private void assignNewAudioPlayer(MediaTrack audioTrack) {
        if (audioTrack != null) {
            activeAudioPlayer = new AudioHtmlPlayer(audioTrack);

            synchPlayState(activeAudioPlayer);
        } else {
            activeAudioPlayer = null;
        }
    }

    private boolean isLive() {
        return raceTimer.getPlayMode() == Timer.PlayModes.Live;
    }

    private void synchPlayState(final MediaPlayer mediaPlayer) {
        mediaPlayer.setPlaybackSpeed(this.raceTimer.getPlaySpeedFactor());
        ensurePlayState(mediaPlayer);
        mediaPlayer.raceTimeChanged(this.raceTimer.getTime());
    }

    private void ensurePlayState(final MediaPlayer mediaPlayer) {
        switch (this.raceTimer.getPlayState()) {
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

    @Override
    public void onClose(CloseEvent<Window> arg0) {
        stopAll();
    }

    @Override
    public void onWindowClosing(ClosingEvent arg0) {
        stopAll();
    }

    @Override
    public void stopAll() {
        if (activeAudioPlayer != null) {
            if (!activeVideoContainers.containsKey(activeAudioPlayer.getMediaTrack())) { // only if audio player isn't
                                                                                         // one of the video players
                                                                                         // anyway.
                activeAudioPlayer.shutDown();
            }
            activeAudioPlayer = null;
        }
        for (VideoContainer videoContainer : new ArrayList<VideoContainer>(activeVideoContainers.values())) { //using a copy to prevent a ConcurrentModificationException
            videoContainer.shutDown();
        }
        activeVideoContainers.clear();
        notifyStateChange();
    }

    @Override
    public void addMediaTrack() {
        TimePoint defaultStartTime = getRaceStartTime();
        if (defaultStartTime == null) {
            defaultStartTime = getTrackingStartTime();
        }
        NewMediaDialog dialog = new NewMediaDialog(mediaService, defaultStartTime,
                MediaPlayerManagerComponent.this.stringMessages, this.getCurrentRace(),
                new DialogCallback<MediaTrack>() {

                    @Override
                    public void cancel() {
                        // no op
                    }

                    @Override
                    public void ok(final MediaTrack mediaTrack) {
                        MediaPlayerManagerComponent.this.getMediaService().addMediaTrack(mediaTrack,
                            new AsyncCallback<String>() {

                                @Override
                                public void onFailure(Throwable t) {
                                    errorReporter.reportError(t.toString());
                                }

                                @Override
                                public void onSuccess(String dbId) {
                                    mediaTrack.dbId = dbId;
                                    assignedMediaTracks.add(mediaTrack);
                                    playFloatingVideo(mediaTrack);
                                    notifyStateChange();
                                }
                        });

                    }
                });
        dialog.show();
    }

    @Override
    public boolean deleteMediaTrack(final MediaTrack mediaTrack) {
        if (Window.confirm(stringMessages.reallyRemoveMediaTrack(mediaTrack.title))) {
            getMediaService().deleteMediaTrack(mediaTrack, new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError(t.toString());
                }

                @Override
                public void onSuccess(Void _void) {
                    MediaPlayerManagerComponent.this.closeFloatingVideo(mediaTrack);
                    assignedMediaTracks.remove(mediaTrack);
                    notifyStateChange();
                }
            });
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean allowsEditing() {
        UserDTO currentUser = userService.getCurrentUser();
        return currentUser != null
                && currentUser.hasPermission(Permission.MANAGE_MEDIA.getStringPermission(),
                        SailingPermissionsForRoleProvider.INSTANCE);
    }

    @Override
    public Boolean isPlaying() {
        return (activeAudioPlayer != null) || (!activeVideoContainers.isEmpty());
    }

    @Override
    public void addPlayerChangeListener(PlayerChangeListener playerChangeListener) {
        this.playerChangeListener.add(playerChangeListener);

    }

    @Override
    public MediaTrack getPlayingAudioTrack() {
        return activeAudioPlayer != null ? activeAudioPlayer.getMediaTrack() : null;
    }

    @Override
    public MediaTrack getDockedVideoTrack() {
        return dockedVideoPlayer != null ? dockedVideoPlayer.getMediaTrack() : null;
    }

    @Override
    public Set<MediaTrack> getPlayingVideoTracks() {
        return activeVideoContainers.keySet();
    }

    @Override
    public Collection<MediaTrack> getAssignedMediaTracks() {
        return Collections.unmodifiableCollection(assignedMediaTracks);
    }
    
    @Override
    public Collection<MediaTrack> getOverlappingMediaTracks() {
        removeMediaTracksWhichAreInAssignedMediaTracks();
        return Collections.unmodifiableCollection(overlappingMediaTracks);
    }

    private void removeMediaTracksWhichAreInAssignedMediaTracks() {
        Collection<MediaTrack> temp = new HashSet<MediaTrack>(overlappingMediaTracks);
        for (MediaTrack mediaTrack : temp) {
            if (assignedMediaTracks.contains(mediaTrack)) {
                overlappingMediaTracks.remove(mediaTrack);
            }
        }
    }

    @Override
    public List<MediaTrack> getVideoTracks() {
        List<MediaTrack> result = new ArrayList<MediaTrack>();
        for (MediaTrack mediaTrack : assignedMediaTracks) {
            if (mediaTrack.mimeType != null && mediaTrack.mimeType.mediaType == MediaType.video) {
                result.add(mediaTrack);
            }
        }
        return result;
    }

    @Override
    public String getLocalizedShortName() {
        return mediaPlayerLifecycle.getLocalizedShortName();
    }

    @Override
    public boolean hasSettings() {
        return mediaPlayerLifecycle.hasSettings();
    }

    @Override
    public SettingsDialogComponent<MediaPlayerSettings> getSettingsDialogComponent(MediaPlayerSettings settings) {
        return mediaPlayerLifecycle.getSettingsDialogComponent(settings);
    }

    @Override
    public void updateSettings(MediaPlayerSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public MediaPlayerSettings getSettings() {
        return settings;
    }

    @Override
    public Widget getEntryWidget() {
        return rootPanel;
    }

    @Override
    public boolean isVisible() {
        return rootPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        rootPanel.setVisible(visibility);
    }

    @Override
    public String getDependentCssClassName() {
        return "media";
    }

    @Override
    public List<MediaTrack> getAudioTracks() {
        List<MediaTrack> result = new ArrayList<MediaTrack>();
        for (MediaTrack mediaTrack : assignedMediaTracks) {
            if (mediaTrack.mimeType != null && mediaTrack.mimeType.mediaType == MediaType.audio) {
                result.add(mediaTrack);
            }
        }
        return result;
    }

    @Override
    public UserAgentDetails getUserAgent() {
        return userAgent;
    }

    @Override
    public RegattaAndRaceIdentifier getCurrentRace() {
        return raceIdentifier;
    }

    @Override
    public MediaServiceAsync getMediaService() {
        return mediaService;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    @Override
    public String getId() {
        return mediaPlayerLifecycle.getComponentId();
    }
}
