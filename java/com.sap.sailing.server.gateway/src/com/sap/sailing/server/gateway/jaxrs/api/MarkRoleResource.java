package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkRoleJsonSerializer;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/markroles")
public class MarkRoleResource extends AbstractSailingServerResource {

    private final JsonSerializer<MarkRole> markRoleSerializer;

    public MarkRoleResource() {
        markRoleSerializer = new MarkRoleJsonSerializer();
    }

    private Response getBadMarkRoleValidationErrorResponse(String errorText) {
        return Response.status(Status.BAD_REQUEST).entity(StringEscapeUtils.escapeHtml(errorText) + ".")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getMarkRoleNotFoundErrorResponse() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getMarkRoles() {
        Iterable<MarkRole> markRolesList = getSharedSailingData().getAllMarkRoles();
        JSONArray result = new JSONArray();
        for (MarkRole markRole : markRolesList) {
            result.add(markRoleSerializer.serialize(markRole));
        }
        final String json = result.toJSONString();
        return Response.ok(json).build();
    }

    @GET
    @Path("{markRoleId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMarkRole(@PathParam("markRoleId") final String markRoleId) {
        final MarkRole markRole = getSharedSailingData().getMarkRoleById(UUID.fromString(markRoleId));
        if (markRole == null) {
            return getMarkRoleNotFoundErrorResponse();
        }
        final JSONObject serializedMarkRole = markRoleSerializer.serialize(markRole);
        final String json = serializedMarkRole.toJSONString();
        return Response.ok(json).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response createMarkRole(@FormParam("name") final String name) {
        if (name == null || name.isEmpty()) {
            return getBadMarkRoleValidationErrorResponse("name must be given");
        }
        final MarkRole markRole = getSharedSailingData().createMarkRole(name);
        final JSONObject serializedMarkRole = markRoleSerializer.serialize(markRole);
        final String json = serializedMarkRole.toJSONString();
        return Response.ok(json).build();
    }
}
