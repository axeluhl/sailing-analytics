package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
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
        String json = result.toJSONString();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}")
    public Response getEvent(@PathParam("eventId") String eventId) {
        return Response.noContent().build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}/racestates")
    public Response getRaceStates(@PathParam("eventId") String eventId) {
        return Response.noContent().build();
    }
}
 