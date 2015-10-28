package com.sap.sailing.gwt.ui.client;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;


/**
 * The async counterpart of {@link HomeService}
 */
public interface HomeServiceAsync {
    void getMediaForEvent(UUID eventId, AsyncCallback<MediaDTO> callback); 
}
