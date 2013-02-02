package com.sap.sailing.racecommittee.app.data;

import java.util.Collection;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public interface ReadonlyDataManager {
	public DataStore getDataStore();
	
	public void loadEvents(LoadClient<Collection<Event>> client);
}
