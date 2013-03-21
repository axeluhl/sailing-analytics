package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class EventsDataHandler extends DataHandler<Collection<EventBase>> {

	public EventsDataHandler(OnlineDataManager manager, LoadClient<Collection<EventBase>> client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Collection<EventBase> data) {
		super.onLoaded(data);
		manager.addEvents(data);
	}

}
