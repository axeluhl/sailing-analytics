package com.sap.sailing.racecommittee.app.data;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;

import android.content.Context;

import com.sap.sailing.domain.base.BoatClass;
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
import com.sap.sailing.racecommittee.app.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.ColorDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.EventJsonDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.FleetDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.RaceCellDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.RaceGroupDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.RaceLogDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.RaceRowDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.SeriesWithRowsDeserializer;
import com.sap.sailing.racecommittee.app.deserialization.impl.VenueJsonDeserializer;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

/**
 * Enables accessing of data.
 */
public class OnlineDataManager extends DataManager {
	// private static final String TAG = OnlineDataManager.class.getName();
	
	private Context context;
	

	OnlineDataManager(Context context, DataStore dataStore) {
		super(dataStore);
		this.context = context;
	}

	public Context getContext() {
		return context;
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
		
		try {
			new DataLoader<Collection<Event>>(
					context, 
					URI.create(TargetHost + "/sailingserver/rc/events"), 
					parser, 
					handler).forceLoad();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadCourseAreas(
			final Serializable parentEventId,
			final LoadClient<Collection<CourseArea>> client) {
		
		if (dataStore.hasEvent(parentEventId)) {
			Event event = dataStore.getEvent(parentEventId);
			client.onLoadSucceded(dataStore.getCourseAreas(event));
		} else {
			reloadEvents(new LoadClient<Collection<Event>>() {
				public void onLoadSucceded(Collection<Event> data) {
					if (dataStore.hasEvent(parentEventId)) {
						Event event = dataStore.getEvent(parentEventId);
						client.onLoadSucceded(dataStore.getCourseAreas(event));
					} else {
						client.onLoadFailed(new DataLoadingException(
								String.format("There was no event object found for id %s.", parentEventId)));
					}
				}
				
				public void onLoadFailed(Exception reason) {
					client.onLoadFailed(new DataLoadingException(
							String.format("There was no event object found for id %s. While reloading the events an error occured: %s", parentEventId, reason), reason));
				}
			});
		}
	}

	public void addRaces(Collection<ManagedRace> data) {
		for (ManagedRace race : data) {
			dataStore.addRace(race);
		}
	}

	public void loadRaces(Serializable courseAreaId, LoadClient<Collection<ManagedRace>> client) {
		
		if (!dataStore.hasCourseArea(courseAreaId)) {
			client.onLoadFailed(new DataLoadingException(String.format("No course area found with id %s", courseAreaId)));
			return;
		}
		
		JsonDeserializer<BoatClass> boatClassDeserializer = new BoatClassJsonDeserializer();
		DataParser<Collection<ManagedRace>> parser =  new ManagedRacesDataParser(
				new RaceGroupDeserializer(
						boatClassDeserializer,
						new SeriesWithRowsDeserializer(
								new RaceRowDeserializer(
										new FleetDeserializer(
												new ColorDeserializer()),
										new RaceCellDeserializer(
												new RaceLogDeserializer())))));
		DataHandler<Collection<ManagedRace>> handler = new ManagedRacesDataHandler(this, client);
		
		try {
			new DataLoader<Collection<ManagedRace>>(
					context, 
					URI.create(TargetHost + "/sailingserver/rc/leaderboards?courseArea=" + courseAreaId.toString()), 
					parser, 
					handler).forceLoad();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static final String TargetHost = "http://192.168.1.142:8888";
	
	

}
