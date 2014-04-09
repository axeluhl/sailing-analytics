package com.sap.sailing.gwt.home.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

@RemoteServiceRelativePath("sailingevents")
public interface SailingEventsService extends RemoteService {

	List<EventDTO> getEvents();

	EventDTO getEventById(String eventId);

}
