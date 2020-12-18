package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public interface MediaServiceWrite extends MediaService {

    MediaTrackWithSecurityDTO addMediaTrack(MediaTrack mediaTrack);

    void deleteMediaTrack(MediaTrack mediaTrack);

    void updateTitle(MediaTrack mediaTrack);

    void updateUrl(MediaTrack mediaTrack);

    void updateStartTime(MediaTrack mediaTrack);

    void updateDuration(MediaTrack mediaTrack);

    void updateRace(MediaTrack mediaTrack);
}
