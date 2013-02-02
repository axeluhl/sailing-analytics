package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Event;
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
		for (Event event : getService().getAllEvents())
		//for (Event event : exampleEvents())
		{
			result.add(eventSerializer.serialize(event));
		}
		result.writeJSONString(response.getWriter());
	}

}
