package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBuilder;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkPropertiesJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/markproperties")
public class MarkPropertiesResource extends AbstractSailingServerResource {

    private Response getBadMarkPropertiesValidationErrorResponse(String errorText) {
        return Response.status(Status.BAD_REQUEST).entity(StringEscapeUtils.escapeHtml(errorText) + ".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getMarkPropertiesNotFoundErrorResponse() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getMarkProperties(@QueryParam("tag") List<String> tags) throws Exception {
        Iterable<MarkProperties> markPropertiesList = getSharedSailingData().getAllMarkProperties(tags);
        JSONArray result = new JSONArray();
        JsonSerializer<MarkProperties> markPropertiesSerializer = new MarkPropertiesJsonSerializer();
        for (MarkProperties markProperties : markPropertiesList) {
            result.add(markPropertiesSerializer.serialize(markProperties));
        }
        final String json = result.toJSONString();
        return Response.ok(json).build();
    }

    @GET
    @Path("{markPositionId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMarkProperties(@PathParam("markPositionId") String markPropertiesId) throws Exception {
        MarkProperties markProperties = getSharedSailingData().getMarkPropertiesById(UUID.fromString(markPropertiesId));
        if (markProperties == null) {
            return getMarkPropertiesNotFoundErrorResponse();
        }
        JsonSerializer<MarkProperties> markPropertiesSerializer = new MarkPropertiesJsonSerializer();
        final JSONObject serializedMarkedProperties = markPropertiesSerializer.serialize(markProperties);
        final String json = serializedMarkedProperties.toJSONString();
        return Response.ok(json).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response createMarkProperties(@FormParam("name") final String name,
            @FormParam("shortName") final String shortName, @FormParam("deviceUuid") String deviceUuid,
            @FormParam("color") String rgbColor, @FormParam("shape") String shape, @FormParam("pattern") String pattern,
            @FormParam("markType") final String markType, @FormParam("tag") List<String> tags,
            @FormParam("latDeg") Double latDeg, @FormParam("lonDeg") Double lonDeg) throws Exception {
        Color color = null;
        if (rgbColor != null && rgbColor.length() > 0) {
            try {
                color = new RGBColor(rgbColor);
            } catch (IllegalArgumentException iae) {
                return getBadMarkPropertiesValidationErrorResponse(String.format("invalid color %s", iae.getMessage()));
            }
        }
        MarkType type = null;
        if (markType != null && markType.length() > 0) {
            type = MarkType.valueOf(markType);
        }
        final MarkPropertiesBuilder markPropertiesBuilder = new MarkPropertiesBuilder(/* id */ null, name, shortName,
                color, shape, pattern, type);
        final MarkProperties createdMarkProperties = getSharedSailingData()
                .createMarkProperties(markPropertiesBuilder.build(), tags);
        if (deviceUuid != null && deviceUuid.length() > 0) {
            final DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString(deviceUuid));
            getSharedSailingData().setTrackingDeviceIdentifierForMarkProperties(createdMarkProperties, device);
        }
        if (latDeg != null && lonDeg != null) {
            final Position fixedPosition = new DegreePosition(latDeg, lonDeg);
            getSharedSailingData().setFixedPositionForMarkProperties(createdMarkProperties, fixedPosition);
        }
        JsonSerializer<MarkProperties> markPropertiesSerializer = new MarkPropertiesJsonSerializer();
        final JSONObject serializedMarkedProperties = markPropertiesSerializer.serialize(createdMarkProperties);
        final String json = serializedMarkedProperties.toJSONString();
        return Response.ok(json).build();
    }

    @PUT
    @Path("{markPositionId}")
    @Produces("application/json;charset=UTF-8")
    public Response updateMarkProperties(@PathParam("markPositionId") String markPositionId,
            @FormParam("deviceUuid") String deviceUuid, @FormParam("latDeg") Double latDeg,
            @FormParam("lonDeg") Double lonDeg) throws Exception {
        MarkProperties markProperties = getSharedSailingData().getMarkPropertiesById(UUID.fromString(markPositionId));
        if (markProperties == null) {
            return getMarkPropertiesNotFoundErrorResponse();
        }
        if (deviceUuid != null && deviceUuid.length() > 0) {
            final DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString(deviceUuid));
            getSharedSailingData().setTrackingDeviceIdentifierForMarkProperties(markProperties, device);
        }
        if (latDeg != null && lonDeg != null) {
            final Position fixedPosition = new DegreePosition(latDeg, lonDeg);
            getSharedSailingData().setFixedPositionForMarkProperties(markProperties, fixedPosition);
        }
        JsonSerializer<MarkProperties> markPropertiesSerializer = new MarkPropertiesJsonSerializer();
        final JSONObject serializedMarkedProperties = markPropertiesSerializer.serialize(markProperties);
        final String json = serializedMarkedProperties.toJSONString();
        return Response.ok(json).build();
    }

    @DELETE
    @Path("{markPositionId}")
    public Response deleteMarkProperties(@PathParam("markPositionId") String markPositionId) throws Exception {
        MarkProperties markProperties = getSharedSailingData().getMarkPropertiesById(UUID.fromString(markPositionId));
        if (markProperties == null) {
            return getMarkPropertiesNotFoundErrorResponse();
        }
        getSharedSailingData().deleteMarkProperties(markProperties);
        return Response.ok().build();
    }
}
