package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.LeaderboardGroupBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;
import com.sap.sse.common.Util.Pair;

@Path("/v1/events")
public class EventsResource extends AbstractSailingServerResource {

    private Response getBadEventErrorResponse(String eventId) {
        return  Response.status(Status.NOT_FOUND).entity("Could not find an event with id '" + eventId + "'.").type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getEvents() {
        JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
        JSONArray result = new JSONArray();
        for (EventBase event : getService().getAllEvents()) {
            if (event.isPublic()) {
                result.add(eventSerializer.serialize(event));
            }
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
            JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(new VenueJsonSerializer(
                    new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
            JSONObject eventJson = eventSerializer.serialize(event);

            String json = eventJson.toJSONString();
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{eventId}/racestates")
    public Response getRaceStates(@PathParam("eventId") String eventId, @QueryParam("filterByLeaderboard") String filterByLeaderboard,
            @QueryParam("filterByCourseArea") String filterByCourseArea, @QueryParam("filterByDayOffset") String filterByDayOffset) {
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
            EventRaceStatesSerializer eventRaceStatesSerializer = new EventRaceStatesSerializer(filterByCourseArea,
                    filterByLeaderboard, filterByDayOffset);
            JSONObject raceStatesJson = eventRaceStatesSerializer.serialize(new Pair<Event, Iterable<Leaderboard>>(event, getService().getLeaderboards().values()));
            String json = raceStatesJson.toJSONString();
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
}
 