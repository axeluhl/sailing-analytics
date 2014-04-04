package com.sap.sailing.gwt.home.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

/**
 * The async counterpart of {@link SailingEventsService}
 */
public interface SailingEventsServiceAsync {

	void getEvents(AsyncCallback<List<EventDTO>> callback);
}
