package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONValue;

import android.content.Context;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.EventsDataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.ManagedRacesDataHandler;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
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
import com.sap.sailing.racecommittee.app.logging.ExLog;

/**
 * Enables accessing of data.
 */
public class DataManager implements ReadonlyDataManager {
	private static final String TAG = DataManager.class.getName();
	
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
				URI.create(TargetHost + "/sailingserver/rc/events"), 
				parser, 
				handler);
		loader.forceLoad();
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
				URI.create(TargetHost + "/sailingserver/rc/leaderboards?courseArea=" + courseAreaId.toString()), 
				parser, 
				handler);
		loader.forceLoad();
		
	}

	public void loadRaceLogs(
			Collection<ManagedRace> data,
			LoadClient<Map<ManagedRace, Collection<RaceLogEvent>>> client) {
		Collection<Serializable> ids = new ArrayList<Serializable>();
		for (ManagedRace race : data) {
			if (!dataStore.hasRace(race.getId())) {
				ExLog.w(TAG, "Tried to load race log for unknown race " + race.getId());
				continue;
			}
			ids.add(race.getId());
		}
		
		HttpJsonPostRequest request = null;
		try {
			String value = JSONValue.toJSONString(ids);
			ExLog.e(TAG, value);
			request = new HttpJsonPostRequest(URI.create(TargetHost + "sailingserver/rc/racelogs"), value);
		} catch (UnsupportedEncodingException e) {
			ExLog.e(TAG, "Exception while trying to create POST request. Aborting:\n" + e);
			return;
		}
		
		/*DataLoader<Map<ManagedRace, Collection<RaceLogEvent>>> loader = 
				new DataLoader<Map<ManagedRace,Collection<RaceLogEvent>>>(
						context,
						request, 
						null, 
						null);*/
	}
	
	private static final String TargetHost = "http://192.168.42.100:8888";
	
	

}
