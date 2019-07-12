package com.sap.sailing.gwt.ui.client;

import java.io.UnsupportedEncodingException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public interface MediaService extends RemoteService {

    Iterable<MediaTrackWithSecurityDTO> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier);

    Iterable<MediaTrackWithSecurityDTO> getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier);

    Iterable<MediaTrackWithSecurityDTO> getAllMediaTracks();

    MediaTrackWithSecurityDTO addMediaTrack(MediaTrack mediaTrack);

    void deleteMediaTrack(MediaTrack mediaTrack);

    void updateTitle(MediaTrack mediaTrack);

    void updateUrl(MediaTrack mediaTrack);

    void updateStartTime(MediaTrack mediaTrack);

    void updateDuration(MediaTrack mediaTrack);

    void updateRace(MediaTrack mediaTrack);

    VideoMetadataDTO checkMetadata(byte[] start, byte[] end, Long skipped);

    VideoMetadataDTO checkMetadata(String url);
    
    /**
     * Obtains a MediaTrack for the given literal url, if one exists, {@code null} otherwise 
     */
    MediaTrack getMediaTrackByUrl(String url);
    
    /**
     * Obtains metadata from the youtube api
     * @throws UnsupportedEncodingException 
     */
    VideoMetadataDTO checkYoutubeMetadata(String url) throws UnsupportedEncodingException;
}
