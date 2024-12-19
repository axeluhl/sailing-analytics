package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.impl.DataAccessWindowDeserializer;
import com.sap.sailing.domain.igtimiadapter.impl.DataAccessWindowSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotDataAccessWindowsResource.DATA_ACCESS_WINDOWS)
public class RiotDataAccessWindowsResource extends AbstractRiotServerResource {
    protected static final String DATA_ACCESS_WINDOWS = "/data_access_windows";
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getDataAccessWindows(@QueryParam("permission") String permission,
            @QueryParam("start_time") String startTime, @QueryParam("end_time") String endTime,
            @QueryParam("serial_numbers[]") Set<String> serialNumbers,
            @QueryParam("stream_ids[]") Set<String> streamIds)
            throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final RiotServer riot = Activator.getInstance().getRiotServer();
        final JSONObject result = new JSONObject();
        final JSONArray dawsJson = new JSONArray();
        result.put("data_access_windows", dawsJson);
        for (final DataAccessWindow daw : riot.getDataAccessWindows()) {
            if (SecurityUtils.getSubject().isPermitted(daw.getIdentifier().getStringPermission(DefaultActions.READ))
                    && serialNumbers.contains(daw.getDeviceSerialNumber())
                    && TimeRange.create(startTime == null ? null : TimePoint.of(Long.valueOf(startTime)),
                                        endTime == null ? null : TimePoint.of(Long.valueOf(endTime))).intersects(daw.getTimeRange())) {
                final JSONObject dataAccessWindowJson = new JSONObject();
                dataAccessWindowJson.put("data_access_window", new DataAccessWindowSerializer().createJsonFromDataAccessWindow(daw));
                dawsJson.add(dataAccessWindowJson);
            }
        }
        return Response.ok(streamingOutput(result)).build();
    }

    @POST
    @Consumes("application/json;charset=UTF-8")
    public Response postDataAccessWindow(@Context HttpServletRequest request) throws IOException, ParseException {
        final RiotServer riot = getRiotService();
        final SecurityService securityService = getSecurityService();
        final InputStream inputStream = request.getInputStream();
        final JSONObject dawJson = (JSONObject) new JSONParser().parse(new InputStreamReader(inputStream));
        final DataAccessWindow daw = new DataAccessWindowDeserializer().createDataAccessWindowFromJson(dawJson);
        securityService.setOwnershipCheckPermissionForObjectCreationAndRevertOnError(SecuredDomainType.IGTIMI_DATA_ACCESS_WINDOW,
                new TypeRelativeObjectIdentifier(Long.toString(daw.getId())), daw.getName(),
                ()->riot.addDataAccessWindow(daw));
        return Response.ok().build();
    }
}
