package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiData.DataPoint.DataCase;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.impl.ResourceDeserializer;
import com.sap.sailing.domain.igtimiadapter.impl.ResourceSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotResourcesResource.RESOURCES)
public class RiotResourcesResource extends AbstractRiotServerResource {
    protected static final String RESOURCES = "/resources";
    private static final String DATA = "/data";
    private static final String LATEST = "/latest";
    
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

    /**
     * Output is the same format as for {@link #getResourcesData(UriInfo)}
     */
    @Path(DATA+LATEST)
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getLatestDatum(
            @QueryParam("serial_numbers[]") Set<String> serialNumbers,
            @QueryParam("type") int type) throws InvalidProtocolBufferException {
        final RiotServer riot = Activator.getInstance().getRiotServer();
        final JSONObject result = new JSONObject();
        for (final String serialNumber : serialNumbers) {
            final Msg lastMessage = riot.getLastMessage(serialNumber, DataCase.forNumber(type));
            if (lastMessage != null) {
                final JSONArray singleMessage = new JSONArray();
                singleMessage.add(Base64.getEncoder().encodeToString(lastMessage.toByteArray()));
                result.put(serialNumber, singleMessage);
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

    /**
     * A response of a successful request contains a {@link JSONObject} where the keys are the device serial numbers,
     * and where the values are arrays of {@link String}s representing Base64-encoded binary protobuf messages that are
     * expected to parse as {@link Msg} objects. There is no guarantee for these messages to be in any specific order.
     */
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
        final Set<DataCase> dataCases = new HashSet<>();
        for (final DataCase dataCase : DataCase.values()) {
            final String typesAndCompressionKey = "types["+dataCase.getNumber()+"]";
            if (queryParams.containsKey(typesAndCompressionKey)) {
                dataCases.add(dataCase);
                final Type type;
                if ((type = Type.valueOf(dataCase.getNumber())) != null) {
                    typesAndCompression.put(type, Double.valueOf(queryParams.getFirst(typesAndCompressionKey)));
                }
            }
        }
        final TimeRange requestedTimeRange = TimeRange.create(startTime, endTime);
        final Iterable<DataAccessWindow> daws = getDataAccessWindowsReadableBySubject(requestedTimeRange, serialNumbers);
        // assemble results from DAWs readable by the user:
        final JSONObject result = new JSONObject();
        final Encoder base64Encoder = Base64.getEncoder();
        for (final DataAccessWindow daw : daws) {
            final TimeRange timeRangeIntersectionBetweenRequestAndDAW = requestedTimeRange.intersection(daw.getTimeRange());
            final Iterable<Msg> messagesForDAW = getRiotService().getMessages(daw.getDeviceSerialNumber(), timeRangeIntersectionBetweenRequestAndDAW, dataCases);
            final JSONArray arrayForDevice = (JSONArray) result.computeIfAbsent(daw.getDeviceSerialNumber(), d->new JSONArray());
            for (final Msg message : messagesForDAW) {
                arrayForDevice.add(new String(base64Encoder.encode(message.toByteArray())));
            }
        }
        return Response.ok(streamingOutput(result)).build();
    }

    private Iterable<DataAccessWindow> getDataAccessWindowsReadableBySubject(TimeRange timeRange, List<String> serialNumbers) {
        final Subject subject = SecurityUtils.getSubject();
        return Util.filter(getRiotService().getDataAccessWindows(serialNumbers, timeRange),
                daw->{
                    return subject.isPermitted(daw.getIdentifier().getStringPermission(DefaultActions.READ));
                });
    }
    
    @Path("{id}")
    @DELETE
    public Response deleteResource(@PathParam("id") long id) throws IOException, ParseException {
        final RiotServer riot = getRiotService();
        final SecurityService securityService = getSecurityService();
        final Resource resource = riot.getResourceById(id);
        if (resource != null) {
            securityService.checkCurrentUserDeletePermission(resource);
            riot.removeResource(id);
            securityService.deleteAllDataForRemovedObject(resource.getIdentifier());
        }
        return Response.ok().build();
    }
}
