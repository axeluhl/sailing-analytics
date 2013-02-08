package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

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
		Event tempEvent = new EventImpl("Kieler Woche 2014 (International)", "Test Location", "", true, "FIXUUID");
		tempEvent.getVenue().addCourseArea(new CourseAreaImpl("Alpha", "FIXCAUUID1"));
		tempEvent.getVenue().addCourseArea(new CourseAreaImpl("Bravo", "FIXCAUUID2"));
		tempEvent.getVenue().addCourseArea(new CourseAreaImpl("Charlie", "FIXCAUUID3"));
		data.add(tempEvent);
		data.add(new EventImpl("Kieler Woche 2014 (Olympisch)", "Test Location", "", true, "DUMBUUID"));
		
		super.onLoaded(data);
		manager.addEvents(data);
	}

}
