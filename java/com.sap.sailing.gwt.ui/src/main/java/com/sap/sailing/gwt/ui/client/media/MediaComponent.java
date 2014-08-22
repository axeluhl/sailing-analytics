package com.sap.sailing.gwt.ui.client.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MediaType;
import com.sap.sailing.domain.common.media.MediaTrack.Status;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayerWithWidget;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sse.gwt.client.player.PlayStateListener;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;

public class MediaComponent implements Component<Void>, PlayStateListener, TimeListener {

    private VideoPlayerWithWidget activeVideoPlayer;
    
    private final SimplePanel rootPanel = new SimplePanel();
    
    private final Collection<MediaTrack> mediaTracks = new ArrayList<MediaTrack>();
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    private final Timer raceTimer;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final UserDTO user;
    private final MediaServiceAsync mediaService;
    private final boolean autoSelectMedia;
    private final String defaultMediaId;
    
    private final MediaSelector mediaSelector;

    private Date currentRaceTime;
    private double currentPlaybackSpeed = 1.0d;
    private PlayStates currentPlayState = PlayStates.Paused;


    public MediaComponent(RegattaAndRaceIdentifier selectedRaceIdentifier, RaceTimesInfoProvider raceTimesInfoProvider,
            Timer raceTimer, MediaServiceAsync mediaService, StringMessages stringMessages,
            ErrorReporter errorReporter, UserAgentDetails userAgent, UserDTO user, boolean autoSelectMedia, String defaultMedia) {
        this.raceIdentifier = selectedRaceIdentifier;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        this.raceTimer = raceTimer;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.user = user;
        this.mediaService = mediaService;
        this.autoSelectMedia = autoSelectMedia;
        this.defaultMediaId = defaultMedia;
        this.mediaSelector = new MediaSelector(selectedRaceIdentifier, raceTimesInfoProvider, raceTimer, mediaService, stringMessages, errorReporter, userAgent, user, autoSelectMedia);
    }
    
    public Button getMediaSelectionButton() {
        return this.mediaSelector.getManageMediaButton();
    }

    public boolean isPotentiallyPlayable(MediaTrack mediaTrack) {
        boolean result = false;
        if (mediaTrack != null) {
            result = MediaTrack.Status.REACHABLE.equals(mediaTrack.status)
                || MediaTrack.Status.UNDEFINED.equals(mediaTrack.status);
        }
        return result;
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
							that.@com.sap.sailing.gwt.ui.client.media.MediaComponent::loadedmetadata(Lcom/sap/sailing/domain/common/media/MediaTrack;)(mediaTrack);
						});
		mediaElement
				.addEventListener(
						'error',
						function() {
							that.@com.sap.sailing.gwt.ui.client.media.MediaComponent::mediaError(Lcom/sap/sailing/domain/common/media/MediaTrack;)(mediaTrack);
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
        videoChanged(defaultVideo);
    }

    public MediaTrack getDefaultVideo() {
        for (MediaTrack mediaTrack : mediaTracks) {
            if (defaultMediaId != null) {
                if (mediaTrack.dbId.equals(defaultMediaId)) {
                    return mediaTrack;
                }
            } else {
                if (MediaType.video.equals(mediaTrack.mimeType.mediaType) && isPotentiallyPlayable(mediaTrack)) {
                    return mediaTrack;
                }
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
        if (activeVideoPlayer != null) {
            activeVideoPlayer.setPlaybackSpeed(this.currentPlaybackSpeed);
        }
    }

    private void pausePlaying() {
        if (activeVideoPlayer != null) {
            activeVideoPlayer.pauseMedia();
        }
    }

    private void startPlaying() {
        if ((activeVideoPlayer != null) && activeVideoPlayer.isCoveringCurrentRaceTime()) {
            activeVideoPlayer.playMedia();
        }
    }

    @Override
    public void timeChanged(Date newRaceTime, Date oldRaceTime) {
        this.currentRaceTime = newRaceTime;
        if (activeVideoPlayer != null) {
            activeVideoPlayer.raceTimeChanged(this.currentRaceTime);
            ensurePlayState();
        }
    }
    
    /**
     * Wraps the callback handling functions in an object to better document their purpose.
     * onSuccess and onError are simply too generic to tell about their concrete use.   
     * @return
     */
    public AsyncCallback<Collection<MediaTrack>> getMediaLibraryCallback() {
        return new  AsyncCallback<Collection<MediaTrack>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getMediaTracksForRace(...) - Failure: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Collection<MediaTrack> mediaTracks) {
                mediaSelector.setMediaTracks(mediaTracks);
                MediaComponent.this.mediaTracks.clear();
                MediaComponent.this.mediaTracks.addAll(mediaTracks);
                for (MediaTrack mediaTrack : MediaComponent.this.mediaTracks) {
                    setStatus(mediaTrack);
                }
                
                if (autoSelectMedia) {
                    setVisible(true);
                }
            }
        };
     }

    public void videoChanged(MediaTrack videoTrack) {
        if (activeVideoPlayer != null) { // --> then reset active audio player

            if (activeVideoPlayer.getMediaTrack() == videoTrack) {
                return; // nothing changed
            }

            activeVideoPlayer = null;
            rootPanel.clear();
        }
        if (videoTrack != null) { 
            if (videoTrack.isYoutube()) {
                activeVideoPlayer = new VideoYoutubePlayer(videoTrack, getRaceStartTime(), false, raceTimer);
            } else {
                activeVideoPlayer = new VideoHtmlPlayer(videoTrack, getRaceStartTime(), false, raceTimer);
            }
            rootPanel.add(activeVideoPlayer.getWidget());
            synchPlayState();
        }
    }

    private long getRaceStartTime() {
        return raceTimesInfoProvider.getRaceTimesInfo(raceIdentifier).startOfRace.getTime();
    }

    private void synchPlayState() {
        if (this.currentRaceTime != null) {
            activeVideoPlayer.setPlaybackSpeed(currentPlaybackSpeed);
            activeVideoPlayer.raceTimeChanged(this.currentRaceTime);
            ensurePlayState();
        }
    }

    private void ensurePlayState() {
        switch (this.currentPlayState) {
        case Playing:
            if (activeVideoPlayer.isMediaPaused() && activeVideoPlayer.isCoveringCurrentRaceTime()) {
                activeVideoPlayer.playMedia();
            }
            break;
        case Paused:
            if (!activeVideoPlayer.isMediaPaused()) {
                activeVideoPlayer.pauseMedia();
            }
        default:
            break;
        }
    }

    private void clear() {
        if (activeVideoPlayer != null) {
            activeVideoPlayer.shutDown();
            activeVideoPlayer = null;
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.videoComponentShortName();
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Void> getSettingsDialogComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSettings(Void newSettings) {
        throw new UnsupportedOperationException();
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
        if (visibility) {
            playDefault();
        }
    }

    @Override
    public String getDependentCssClassName() {
        return "media";
    }

}
