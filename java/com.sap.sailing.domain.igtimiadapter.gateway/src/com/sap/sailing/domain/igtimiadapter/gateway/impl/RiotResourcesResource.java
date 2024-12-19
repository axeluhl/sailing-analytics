package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;
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

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.impl.ResourceSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotResourcesResource.RESOURCES)
public class RiotResourcesResource extends AbstractRiotServerResource {
    protected static final String RESOURCES = "/resources";
    private static final String DATA = "/data";
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getResources(@QueryParam("permission") String permission,
            @QueryParam("start_time") String startTime, @QueryParam("end_time") String endTime,
            @QueryParam("serial_numbers[]") Set<String> serialNumbers,
            @QueryParam("stream_ids[]") Set<String> streamIds)
            throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final RiotServer riot = Activator.getInstance().getRiotServer();
        final JSONObject result = new JSONObject();
        final JSONArray resourcesJson = new JSONArray();
        result.put("resources", resourcesJson);
        for (final Resource resource : riot.getResources()) {
            if (SecurityUtils.getSubject().isPermitted(resource.getIdentifier().getStringPermission(DefaultActions.READ))
             && serialNumbers.contains(resource.getDeviceSerialNumber())
             && TimeRange.create(startTime == null ? null : TimePoint.of(Long.valueOf(startTime)),
                                 endTime == null ? null : TimePoint.of(Long.valueOf(endTime))).intersects(
                                         resource.getTimeRange())) {
                resourcesJson.add(new ResourceSerializer().createJsonFromResource(resource));
            }
        }
        return Response.ok(streamingOutput(result)).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
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
