package com.sap.sailing.racecommittee.app.data.clients;

import java.util.Collection;

import com.sap.sailing.domain.base.Event;

public interface EventsLoadClient extends LoadClient<Event> {
	public void onEventsLoaded(Collection<Event> events);
}
