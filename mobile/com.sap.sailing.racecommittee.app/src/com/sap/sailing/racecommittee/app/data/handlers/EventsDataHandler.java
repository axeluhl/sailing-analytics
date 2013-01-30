package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.EventsLoadClient;

public class EventsDataHandler extends DataHandler<Collection<Event>> {

	public EventsDataHandler(DataManager manager, EventsLoadClient client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Collection<Event> data) {
		super.onLoaded(data);
		// TODO: add to manager!
		((EventsLoadClient)client).onEventsLoaded(data);
	}

}
