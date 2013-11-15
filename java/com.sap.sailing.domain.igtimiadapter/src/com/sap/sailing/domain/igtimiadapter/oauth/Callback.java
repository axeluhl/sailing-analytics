package com.sap.sailing.domain.igtimiadapter.oauth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/v1/authorizationcallback")
public class Callback {
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/")
    public Response getEvent(@QueryParam("access_token") String accessToken) {
        return Response.ok().build();
    }
}
