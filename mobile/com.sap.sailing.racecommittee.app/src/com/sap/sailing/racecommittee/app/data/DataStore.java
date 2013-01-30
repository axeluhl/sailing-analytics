package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sailing.domain.base.Event;

public interface DataStore {

	public Collection<Event> getEvents();
	public Event getEvent(Serializable id);
	
}
