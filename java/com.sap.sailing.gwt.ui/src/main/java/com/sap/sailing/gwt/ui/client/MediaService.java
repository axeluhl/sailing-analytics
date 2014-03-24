package com.sap.sailing.gwt.ui.client;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;

public interface MediaService extends RemoteService {

    Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier);

    Collection<MediaTrack> getAllMediaTracks();

    String addMediaTrack(MediaTrack mediaTrack);

    void deleteMediaTrack(MediaTrack mediaTrack);

    void updateTitle(MediaTrack mediaTrack);

    void updateUrl(MediaTrack mediaTrack);

    void updateStartTime(MediaTrack mediaTrack);

    void updateDuration(MediaTrack mediaTrack);

}
