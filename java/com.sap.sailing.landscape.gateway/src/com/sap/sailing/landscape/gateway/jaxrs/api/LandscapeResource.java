package com.sap.sailing.landscape.gateway.jaxrs.api;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.sap.sailing.landscape.LandscapeService;
import com.sap.sailing.landscape.gateway.jaxrs.AbstractLandscapeResource;

@Path("/landscape")
public class LandscapeResource extends AbstractLandscapeResource {
    @GET
    @Produces("application/octet-stream;charset=UTF-8")
    public Response getRegressions() throws IOException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                LandscapeService landscapeService = getLandscapeService();
                output.write(landscapeService.helloWorld().getBytes());
            }
        }).header("Content-Type", "application/octet-stream").build();
    }
}
