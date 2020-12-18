package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public interface MediaServiceWriteAsync extends MediaServiceAsync {

    void addMediaTrack(MediaTrack mediaTrack, AsyncCallback<MediaTrackWithSecurityDTO> asyncCallback);

    void deleteMediaTrack(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateTitle(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateUrl(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateStartTime(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateDuration(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

    void updateRace(MediaTrack mediaTrack, AsyncCallback<Void> asyncCallback);

}
