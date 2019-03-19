package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CountryCodeJsonSerializer;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;

@Path("/v1/countrycodes")
public class CountryCodesResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getCountryCodes() {
        final JSONArray result = new JSONArray();
        final CountryCodeFactory factory = CountryCodeFactory.INSTANCE;
        final JsonSerializer<CountryCode> serializer = CountryCodeJsonSerializer.create();
        for (final CountryCode cc : factory.getAll()) {
            result.add(serializer.serialize(cc));
        }
        return Response.ok(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
}
