package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

public class EventDataJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 4515246650108245796L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonSerializer<EventBase> eventSerializer = createSerializer();
        JSONArray result = new JSONArray();
        for (EventBase event : getService().getAllEvents()) {
            result.add(eventSerializer.serialize(event));
        }
        result.writeJSONString(response.getWriter());
        response.setContentType("application/json");
    }

    private static JsonSerializer<EventBase> createSerializer() {
        return new EventDataJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()));
    }

}
