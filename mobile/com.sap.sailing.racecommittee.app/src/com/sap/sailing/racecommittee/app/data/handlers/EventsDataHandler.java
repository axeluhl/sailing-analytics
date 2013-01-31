package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class EventsDataHandler extends DataHandler<Collection<Event>> {

	public EventsDataHandler(DataManager manager, LoadClient<Collection<Event>> client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Collection<Event> data) {
		super.onLoaded(data);
		manager.addEvents(data);
	}

}
