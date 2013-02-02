package com.sap.sailing.racecommittee.app.data;

import java.net.URI;
import java.util.Collection;

import android.content.Context;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.EventsDataHandler;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoader;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.data.parsers.EventsDataParser;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;

/**
 * Enables accessing of data.
 */
public class DataManager implements ReadonlyDataManager {
	
	public static ReadonlyDataManager create(Context context, DataStore dataStore) {
		return new DataManager(context, dataStore);
	}
	
	private Context context;
	private DataStore dataStore;

	private DataManager(Context context, DataStore dataStore) {
		this.context = context;
		this.dataStore = dataStore;
	}

	public Context getContext() {
		return context;
	}
	
	public DataStore getDataStore() {
		return dataStore;
	}
	
	public void loadEvents(LoadClient<Collection<Event>> client) {
		if (dataStore.getEvents().isEmpty()) {
			reloadEvents(client);
		} else {
			client.onLoadSucceded(dataStore.getEvents());
		}
	}

	public void addEvents(Collection<Event> events) {
		for (Event event : events) {
			if (!dataStore.hasEvent(event.getId())) {
				dataStore.addEvent(event);
			}
		}
	}

	protected void reloadEvents(LoadClient<Collection<Event>> client) {
		DataParser<Collection<Event>> parser = new EventsDataParser(
				new EventJsonDeserializer(
					new VenueJsonDeserializer(
							new CourseAreaJsonDeserializer())));
		DataHandler<Collection<Event>> handler = new EventsDataHandler(this, client);
		
		DataLoader<Collection<Event>> loader = new DataLoader<Collection<Event>>(
				context, 
				URI.create("http://10.0.2.2:8888/sailingserver/rc/events"), 
				parser, 
				handler);
		loader.forceLoad();
	}
	
	

}
