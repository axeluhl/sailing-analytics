package com.sap.sailing.gwt.ui.client;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;

public interface MediaServiceAsync {

    void getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier,
            AsyncCallback<Collection<MediaTrack>> callback);
    
    void getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier,
            AsyncCallback<Collection<MediaTrack>> callback);

    void getAllMediaTracks(AsyncCallback<Collection<MediaTrack>> asyncCallback);

    void addMediaTrack(MediaTrack mediaTrack, AsyncCallback<String> asyncCallback);

    void deleteMediaTrack(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateTitle(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateUrl(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateStartTime(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateDuration(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);
    
    void updateRace(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

}
