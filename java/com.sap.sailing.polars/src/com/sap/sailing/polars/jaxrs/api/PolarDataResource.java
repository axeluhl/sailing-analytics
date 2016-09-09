package com.sap.sailing.polars.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sap.sailing.polars.jaxrs.AbstractPolarResource;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response get() {
        String json = "{\"id\": 9}";
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
}
