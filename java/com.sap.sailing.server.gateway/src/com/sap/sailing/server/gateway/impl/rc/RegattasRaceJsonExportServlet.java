package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceDefinitionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;


public class RegattasRaceJsonExportServlet extends JsonExportServlet {

	private static final long serialVersionUID = -5661776042560467182L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Event event = getEvent(request);
		if (event == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		RegattaJsonSerializer serializer = new RegattaJsonSerializer(
				new BoatClassJsonSerializer(),
				new RaceDefinitionJsonSerializer(null));
		JSONArray result = new JSONArray();
		for (Regatta regatta : event.getRegattas())
		{
			result.add(serializer.serialize(regatta));
		}
		result.writeJSONString(response.getWriter());
	}

	private Event getEvent(HttpServletRequest request) {
		// PathInfo is prefixed by forward slash
		String pathInfo = request.getPathInfo();
		if (pathInfo.length() < 2)
		{
			return null;
		}
		
		// Strip leading slash...
		pathInfo = pathInfo.substring(1);
		
		try {
			// ... and get event!
			Serializable id = getEventId(pathInfo);
			return getService().getEvent(id);
		}
		catch (IllegalArgumentException iae)
		{
			return null;
		}
	}

	private Serializable getEventId(String idValue) {
		return UUID.fromString(idValue);
	}

}
