package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.UUID;

import javax.ws.rs.DELETE;
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

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBuilder;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/markproperties")
public class MarkPropertiesResource extends AbstractSailingServerResource {

    private Response getBadMarkPropertiesValidationErrorResponse(String errorText) {
        return Response.status(Status.BAD_REQUEST).entity(StringEscapeUtils.escapeHtml(errorText) + ".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getMarkProperties() throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @GET
    @Path("{markPositionId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMarkProperties(@PathParam("markPositionId") UUID markPositionId) throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response createMarkProperties(@QueryParam("name") final String name,
            @QueryParam("shortName") final String shortName, @QueryParam("deviceUuid") String deviceUuid,
            @QueryParam("color") String rgbColor) throws Exception {
        Color color = null;
        if (rgbColor != null && rgbColor.length() > 0) {
            try {
                color = new RGBColor(rgbColor);
            } catch (IllegalArgumentException iae) {
                return getBadMarkPropertiesValidationErrorResponse(String.format("invalid color %s", iae.getMessage()));
            }
        }
        MarkPropertiesBuilder markPropertiesBuilder = new MarkPropertiesBuilder(null, name, shortName, color, "test",
                "", MarkType.STARTBOAT);
        if (deviceUuid != null && deviceUuid.length() > 0) {
            final DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString(deviceUuid));
            markPropertiesBuilder.withDeviceId(device);
        }
        MarkProperties markProperties = this.getSharedSailingData().createMarkProperties(markPropertiesBuilder.build(),
                new ArrayList<String>());
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("{markPositionId}")
    @Produces("application/json;charset=UTF-8")
    public Response updateMarkProperties(@PathParam("markPositionId") UUID madkPositionId) throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @DELETE
    @Path("{markPositionId}")
    public Response deleteMarkProperties(@PathParam("markPositionId") UUID madkPositionId) throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }
}
