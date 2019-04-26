package com.sap.sailing.gwt.ui.client.media;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public interface MediaPlayerManager {
    
    public interface PlayerChangeListener {

        void notifyStateChange();

    }
    
    public enum Status {
        UNDEFINED('?'), CANNOT_PLAY('-'), NOT_REACHABLE('#'), REACHABLE('+');

        private final char symbol;

        private Status(char symbol) {
            this.symbol = symbol;
        }

        public String toString() {
            return String.valueOf(this.symbol);
        }

        public boolean isPotentiallyPlayable() {
            return this == UNDEFINED || this == REACHABLE;
        }
    }

    void addPlayerChangeListener(PlayerChangeListener playerChangeListener);
    
    void playAudio(MediaTrack audioTrack);

    void muteAudio();
    
    void playFloatingVideo(MediaTrack videoTrack);
    
    void closeFloatingPlayer(MediaTrack videoTrack);
    
    void playDockedVideo(MediaTrack videoTrack);
    
    void closeDockedVideo();
    
    void addMediaTrack();
    
    boolean deleteMediaTrack(MediaTrack mediaTrack);
    
    boolean allowsEditing(String mediaTrackDbId);

    boolean allowsCreating();

    void playDefault();

    void stopAll();

    Boolean isPlaying();

    Set<MediaTrack> getPlayingAudioTrack();

    Set<MediaTrack> getPlayingVideoTracks();

    Collection<MediaTrack> getAssignedMediaTracks();
    
    Collection<MediaTrack> getOverlappingMediaTracks();

    List<MediaTrack> getAudioTracks();

    List<MediaTrack> getVideoTracks();

    MediaTrack getDockedVideoTrack();

    UserAgentDetails getUserAgent();

    RegattaAndRaceIdentifier getCurrentRace();

    MediaServiceAsync getMediaService();

    ErrorReporter getErrorReporter();

    Status getMediaTrackStatus(MediaTrack track);

}

