package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;

@Path("/v1/ping")
public class PingResource extends AbstractSailingServerResource {
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response ping() {
        return Response.ok().build();
    }
}
