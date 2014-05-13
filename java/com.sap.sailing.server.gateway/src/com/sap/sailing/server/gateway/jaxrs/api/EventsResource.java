package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

@Path("/v1/events")
public class EventsResource extends AbstractSailingServerResource {

    private Response getBadEventErrorResponse(String eventId) {
        return  Response.status(Status.NOT_FOUND).entity("Could not find an event with id '" + eventId + "'.").type(MediaType.TEXT_PLAIN).build();
    }

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
        Response response;
        UUID eventUuid;
        try {
			eventUuid = UUID.fromString(eventId);
		} catch (IllegalArgumentException e) {
			return getBadEventErrorResponse(eventId);
		}
        
        Event event = getService().getEvent(eventUuid);
        if (event == null) {
            response = getBadEventErrorResponse(eventId);
        } else {
            JsonSerializer<EventBase> eventSerializer = new EventJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()));
            JSONObject eventJson = eventSerializer.serialize(event);

            String json = eventJson.toJSONString();
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}/racestates")
    public Response getRaceStates(@PathParam("eventId") String eventId) {
        return Response.noContent().build();
    }
}
 