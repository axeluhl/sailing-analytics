package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotDevicesResource.DEVICES)
public class RiotDevicesResource {
    protected static final String DEVICES = "/devices";
    
    @GET
    @Produces("text/plain;charset=UTF-8")
    public Response getDevices(@QueryParam("permission") String permission,
            @QueryParam("start_time") String start_time, @QueryParam("end_time") String endTime,
            @QueryParam("serial_numbers[]") Set<String> serialNumbers, @QueryParam("stream_ids[]") Set<String> streamIds) {
        return Response.ok().build(); // TODO implement getResources(...)
    }
}
