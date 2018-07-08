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
    
    void addPlayerChangeListener(PlayerChangeListener playerChangeListener);
    
    void playAudio(MediaTrack audioTrack);

    void muteAudio();
    
    void playFloatingVideo(MediaTrack videoTrack);
    
    void closeFloatingVideo(MediaTrack videoTrack);
    
    void playDockedVideo(MediaTrack videoTrack);
    
    void closeDockedVideo();
    
    void addMediaTrack();
    
    boolean deleteMediaTrack(MediaTrack mediaTrack);
    
    boolean allowsEditing();

    void playDefault();

    void stopAll();

    Boolean isPlaying();

    MediaTrack getPlayingAudioTrack();

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

}

