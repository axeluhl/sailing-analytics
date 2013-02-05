package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;

import android.content.Context;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.EventsDataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.ManagedRacesDataHandler;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoader;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.data.parsers.EventsDataParser;
import com.sap.sailing.racecommittee.app.data.parsers.ManagedRacesDataParser;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.ColorDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.EventJsonDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.FleetDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.FleetWithRaceNamesDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.RaceGroupDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.SeriesDataDeserializer;
import com.sap.sailing.racecommittee.app.domain.deserialization.impl.VenueJsonDeserializer;

/**
 * Enables accessing of data.
 */
public class DataManager implements ReadonlyDataManager {
	
	public static ReadonlyDataManager create(Context context) {
		return new DataManager(context, InMemoryDataStore.INSTANCE);
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
			dataStore.addEvent(event);
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
				URI.create("http://192.168.178.22:8888/sailingserver/rc/events"), 
				parser, 
				handler);
		loader.forceLoad();
	}

	public void loadCourseAreas(Serializable parentEventId,
			LoadClient<Collection<CourseArea>> client) {
		
		if (dataStore.hasEvent(parentEventId)) {
			Event event = dataStore.getEvent(parentEventId);
			client.onLoadSucceded(dataStore.getCourseAreas(event));
		} else {
			client.onLoadFailed(new IllegalStateException("Unknown event id..."));
		}
		
	}

	public void addRaces(Collection<ManagedRace> data) {
		for (ManagedRace race : data) {
			dataStore.addRace(race);
		}
	}

	public void loadRaces(Serializable courseAreaId, LoadClient<Collection<ManagedRace>> client) {
		
		if (!dataStore.hasCourseArea(courseAreaId)) {
			client.onLoadFailed(new IllegalStateException("No course area found with given id"));
			return;
		}
		
		//JsonDeserializer<BoatClass> boatClassDeserializer = new BoatClassJsonDeserializer();
		DataParser<Collection<ManagedRace>> parser =  new ManagedRacesDataParser(
				new RaceGroupDeserializer(
						new SeriesDataDeserializer(
								new FleetWithRaceNamesDeserializer(
										new FleetDeserializer(
												new ColorDeserializer())))));
		DataHandler<Collection<ManagedRace>> handler = new ManagedRacesDataHandler(this, client);
		
		DataLoader<Collection<ManagedRace>> loader = new DataLoader<Collection<ManagedRace>>(
				context, 
				URI.create("http://192.168.178.22:8888/sailingserver/rc/leaderboards?courseArea=" + courseAreaId.toString()), 
				parser, 
				handler);
		loader.forceLoad();
		
	}
	
	

}
