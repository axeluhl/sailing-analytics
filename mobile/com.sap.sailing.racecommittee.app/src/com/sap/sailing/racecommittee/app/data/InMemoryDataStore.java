package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.domain.base.Event;

public enum InMemoryDataStore implements DataStore {
	INSTANCE;
	
	private HashMap<Serializable, Event> eventsById;

	public Collection<Event> getEvents() {
		return eventsById.values();
	}

	public Event getEvent(Serializable id) {
		return eventsById.get(id);
	}
}
