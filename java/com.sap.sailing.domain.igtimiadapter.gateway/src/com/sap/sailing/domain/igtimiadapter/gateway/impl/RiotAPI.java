package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.gateway.oauth.RestApiApplication;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotAPI.RESOURCES)
public class RiotAPI {
    protected static final String RESOURCES = "/resources";
    private static final String DATA = "/data";
    
    @GET
    @Produces("text/plain;charset=UTF-8")
    public Response getResources(@QueryParam("permission") String permission,
            @QueryParam("start_time") String start_time, @QueryParam("end_time") String endTime,
            @QueryParam("serial_numbers[]") Set<String> serialNumbers, @QueryParam("stream_ids[]") Set<String> streamIds) {
        return Response.ok().build(); // TODO implement getResources(...)
    }

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path(DATA)
    public Response getResourcesData(@Context UriInfo ui) {
        final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        final String startTime = queryParams.getFirst("start_time");
        final String endTime = queryParams.getFirst("end_time");
        final List<String> serialNumbers = queryParams.get("serial_numbers[]");
        final Boolean restoreArchies = queryParams.containsKey("restore_archives") ? Boolean.valueOf(queryParams.getFirst("restore_archives")) : null;
        final Map<Type, Double> typesAndCompression = new HashMap<>();
        for (final Type type : Type.values()) {
            final String typesAndCompressionKey = "types["+type.getCode()+"]";
            if (queryParams.containsKey(typesAndCompressionKey)) {
                typesAndCompression.put(type, Double.valueOf(queryParams.getFirst(typesAndCompressionKey)));
            }
        }
        return Response.ok().build(); // TODO implement getResourcesData(...)
    }
}
