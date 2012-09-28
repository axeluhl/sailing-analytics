package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

@RemoteServiceRelativePath("media")
public interface MediaService extends RemoteService {

    Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier);

    List<MediaTrack> getAllMediaTracks();

    void addMediaTrack(MediaTrack mediaTrack);

    void deleteMediaTrack(MediaTrack mediaTrack);

}
