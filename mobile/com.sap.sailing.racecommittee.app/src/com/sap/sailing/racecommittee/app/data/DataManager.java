package com.sap.sailing.racecommittee.app.data;

import android.content.Context;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.data.clients.EventsLoadClient;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;

/**
 * Enables accessing of all data
 */
public class DataManager {
	
	private Context context;
	private DataStore dataStore;

	public DataManager(Context context, DataStore dataStore) {
		this.context = context;
		this.dataStore = dataStore;
	}

	public Context getContext() {
		return context;
	}
	
	public void getEvents(EventsLoadClient client) {
		if (dataStore.getEvents().isEmpty()) {
			loadEvents(client);
		} else {
			client.onEventsLoaded(dataStore.getEvents());
		}
	}

	protected void loadEvents(EventsLoadClient client) {
		// TODO Auto-generated method stub
		DataHandler<Event> handler = null;
	}
	
	

}
