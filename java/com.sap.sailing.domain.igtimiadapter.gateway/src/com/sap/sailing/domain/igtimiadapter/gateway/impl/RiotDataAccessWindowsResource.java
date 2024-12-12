package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotDataAccessWindowsResource.DATA_ACCESS_WINDOWS)
public class RiotDataAccessWindowsResource {
    protected static final String DATA_ACCESS_WINDOWS = "/data_access_windows";
    
    @GET
    @Produces("text/plain;charset=UTF-8")
    public Response getDataAccessWindows(@QueryParam("permission") String permission,
            @QueryParam("start_time") String start_time, @QueryParam("end_time") String endTime,
            @QueryParam("serial_numbers[]") Set<String> serialNumbers, @QueryParam("stream_ids[]") Set<String> streamIds) {
        return Response.ok().build(); // TODO implement getDataAccessWindows(...)
    }
}
