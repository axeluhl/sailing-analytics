package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class EventsDataHandler extends DataHandler<Collection<EventData>> {

	public EventsDataHandler(OnlineDataManager manager, LoadClient<Collection<EventData>> client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Collection<EventData> data) {
		super.onLoaded(data);
		manager.addEvents(data);
	}

}
