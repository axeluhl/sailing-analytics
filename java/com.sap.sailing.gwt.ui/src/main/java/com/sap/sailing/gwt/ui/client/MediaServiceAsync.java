package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public interface MediaServiceAsync {

    void getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier,
            AsyncCallback<Iterable<MediaTrack>> callback);

    void getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier,
            AsyncCallback<Iterable<MediaTrack>> callback);

    void getAllMediaTracks(AsyncCallback<Iterable<MediaTrackWithSecurityDTO>> asyncCallback);

    void addMediaTrack(MediaTrack mediaTrack, AsyncCallback<String> asyncCallback);

    void deleteMediaTrack(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateTitle(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateUrl(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateStartTime(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateDuration(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateRace(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void checkMetadata(byte[] jStart, byte[] jEnd, Long skipped, AsyncCallback<VideoMetadataDTO> asyncCallback);

    void checkMetadata(String url, AsyncCallback<VideoMetadataDTO> asyncCallback);

    void getMediaTrackByUrl(String url, AsyncCallback<MediaTrack> asyncCallback);

    void checkYoutubeMetadata(String url, AsyncCallback<VideoMetadataDTO> asyncCallback);

}
