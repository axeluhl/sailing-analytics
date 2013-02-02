package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

public class EventsJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = 4515246650108245796L;
	
	/**
	 * Created in init() - access is thread-safe.
	 */
	private JsonSerializer<Event> eventSerializer;
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		eventSerializer = new EventJsonSerializer(
				new VenueJsonSerializer(
						new CourseAreaJsonSerializer()));
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		JSONArray result = new JSONArray();
		//for (Event event : getService().getAllEvents())
		for (Event event : exampleEvents())
		{
			result.add(eventSerializer.serialize(event));
		}
		result.writeJSONString(response.getWriter());
	}
	
	private Iterable<Event> exampleEvents() 
	{
		Event kielerWocheEvent = getService().addEvent(
				"Kieler Woche 2012 - Where Sailing meets people", 
				"Schilksee", 
				"http://www.youtube.com", 
				true, Collections.<String>emptyList());
				
		Venue schilksee = kielerWocheEvent.getVenue();
		
		CourseArea alpha = new CourseAreaImpl("Alpha", UUID.randomUUID());
		schilksee.addCourseArea(alpha);
		
		//Fleet allBoats = new FleetImpl("Blue");
		//Series series = null;//new SeriesData("Qualification", false, Arrays.asList(allBoats));
		
		Regatta regatta = getService().getOrCreateRegatta(
				"470er Men", 
				"470er",
				UUID.fromString("b2775b10-5b3f-11e2-bcfd-0800200c9a66"));
		kielerWocheEvent.addRegatta(regatta);
		
		RaceDefinition myRace = new RaceDefinitionImpl(
				"Race One", 
				new CourseImpl("Downwind", Collections.<Waypoint>emptyList()), 
				regatta.getBoatClass(), 
				Collections.<Competitor>emptyList(), 
				UUID.fromString("c2775b10-5b3f-11e2-bcfd-0800200c9a66"));
		regatta.addRace(myRace);
		
		
		return Collections.<Event>singleton(kielerWocheEvent);
	}

}
