package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;
import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class EventsDataHandler extends DataHandler<Collection<Event>> {

	public EventsDataHandler(DataManager manager, LoadClient<Collection<Event>> client) {
		super(manager, client);
	}
	
	@Override
	public void onLoaded(Collection<Event> data) {
		Event testEvet = new EventImpl("Test Event", "Berlin", "http://example.com", true, UUID.randomUUID());
		testEvet.getVenue().addCourseArea(new CourseAreaImpl("Test Area", UUID.randomUUID()));
		data.add(testEvet);
		
		super.onLoaded(data);
		manager.addEvents(data);
	}

}
