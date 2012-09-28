package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public interface MediaServiceAsync {

    void getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier,
            AsyncCallback<Collection<MediaTrack>> callback);

    void getAllMediaTracks(AsyncCallback<List<MediaTrack>> asyncCallback);

    void addMediaTrack(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void deleteMediaTrack(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

}
