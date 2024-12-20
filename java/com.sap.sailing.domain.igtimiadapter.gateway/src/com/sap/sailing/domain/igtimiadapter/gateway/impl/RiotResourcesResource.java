package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.impl.ResourceDeserializer;
import com.sap.sailing.domain.igtimiadapter.impl.ResourceSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

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
             && (serialNumbers.isEmpty() || serialNumbers.contains(resource.getDeviceSerialNumber()))
             && TimeRange.create(startTime == null ? null : TimePoint.of(Long.valueOf(startTime)),
                                 endTime == null ? null : TimePoint.of(Long.valueOf(endTime))).intersects(
                                         resource.getTimeRange())) {
                resourcesJson.add(new ResourceSerializer().createJsonFromResource(resource));
            }
        }
        return Response.ok(streamingOutput(result)).build();
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    public Response postResource(@Context HttpServletRequest request) throws IOException, ParseException {
        final RiotServer riot = getRiotService();
        final SecurityService securityService = getSecurityService();
        final InputStream inputStream = request.getInputStream();
        final JSONObject resourceJson = (JSONObject) new JSONParser().parse(new InputStreamReader(inputStream));
        final Resource resource = new ResourceDeserializer().createResourceFromJson(resourceJson);
        securityService.setOwnershipCheckPermissionForObjectCreationAndRevertOnError(SecuredDomainType.IGTIMI_RESOURCE,
                new TypeRelativeObjectIdentifier(Long.toString(resource.getId())), resource.getName(),
                ()->riot.addResource(resource));
        return Response.ok().build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path(DATA)
    public Response getResourcesData(@Context UriInfo ui) {
        final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        final String startTimeString = queryParams.getFirst("start_time");
        final TimePoint startTime = startTimeString == null ? null : TimePoint.of(Long.valueOf(startTimeString));
        final String endTimeString = queryParams.getFirst("end_time");
        final TimePoint endTime = endTimeString == null ? null : TimePoint.of(Long.valueOf(endTimeString));
        final List<String> serialNumbers = queryParams.get("serial_numbers[]");
        // The "restore_archives" parameter was part of the original Riot API, but we currently don't implement any archiving
        // procedure for the resource messages, so we ignore it.
        // final Boolean restoreArchives = queryParams.containsKey("restore_archives") ? Boolean.valueOf(queryParams.getFirst("restore_archives")) : null;
        final Map<Type, Double> typesAndCompression = new HashMap<>();
        for (final Type type : Type.values()) {
            final String typesAndCompressionKey = "types["+type.getCode()+"]";
            if (queryParams.containsKey(typesAndCompressionKey)) {
                typesAndCompression.put(type, Double.valueOf(queryParams.getFirst(typesAndCompressionKey)));
            }
        }
        final Iterable<DataAccessWindow> daws = getDataAccessWindowsReadableBySubject(startTime, endTime, serialNumbers);
        if (SecurityUtils.getSubject().isPermitted(resource.getIdentifier().getStringPermission(DefaultActions.READ))
                && (serialNumbers.isEmpty() || serialNumbers.contains(resource.getDeviceSerialNumber()))
                && TimeRange.create(startTime == null ? null : TimePoint.of(Long.valueOf(startTime)),
                                    endTime == null ? null : TimePoint.of(Long.valueOf(endTime))).intersects(
                                            resource.getTimeRange())) {

        return Response.ok().build(); // TODO implement getResourcesData(...)
    }
}
