package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/v1/markproperties")
public class MarkPropertiesResource extends AbstractSailingServerResource {

    @GET
    @Path("")
    @Produces("application/json;charset=UTF-8")
    public Response getMarkPositions() throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @GET
    @Path("{markPositionId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMarkPosition(@PathParam("markPositionId") UUID markPositionId) throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response createMarkPosition() throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @PUT
    @Path("{markPositionId}")
    @Produces("application/json;charset=UTF-8")
    public Response updateMarkPosition(@PathParam("markPositionId") UUID madkPositionId) throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    @DELETE
    @Path("{markPositionId}")
    public Response deleteMarkPosition(@PathParam("markPositionId") UUID madkPositionId) throws Exception {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }
}
