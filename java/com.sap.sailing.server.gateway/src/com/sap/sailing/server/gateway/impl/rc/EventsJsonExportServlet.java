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
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		JsonSerializer<Event> eventSerializer = createSerializer();
		JSONArray result = new JSONArray();
		for (Event event : getService().getAllEvents())
		{
			result.add(eventSerializer.serialize(event));
		}
		result.writeJSONString(response.getWriter());
	}

	private static JsonSerializer<Event> createSerializer() {
		return new EventJsonSerializer(
				new VenueJsonSerializer(
						new CourseAreaJsonSerializer()));
	}

}
