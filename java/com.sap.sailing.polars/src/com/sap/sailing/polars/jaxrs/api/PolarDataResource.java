package com.sap.sailing.polars.jaxrs.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sap.sailing.polars.jaxrs.AbstractPolarResource;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {
    @GET
    @Produces("application/octet-stream;charset=UTF-8")
    public Response getRegressions() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        getPolarDataServiceImpl().serializeForInitialReplication(bos);
        bos.close();
        return Response.ok(new ByteArrayInputStream(bos.toByteArray())).header("Content-Type", "application/octet-stream").build();
    }
}
