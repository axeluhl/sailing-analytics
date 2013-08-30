package com.sap.sailing.server.gateway.impl.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

@Path("/v1/events")
public class EventsResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getEvents() {
        JsonSerializer<EventBase> eventSerializer = new EventJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()));
        JSONArray result = new JSONArray();
        for (EventBase event : getService().getAllEvents()) {
            result.add(eventSerializer.serialize(event));
        }
        byte[] json = result.toJSONString().getBytes();
        return Response.ok(json).build();
    }
    
}
 