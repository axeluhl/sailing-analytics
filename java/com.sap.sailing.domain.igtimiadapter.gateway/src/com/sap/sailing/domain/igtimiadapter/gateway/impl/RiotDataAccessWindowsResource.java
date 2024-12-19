package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.impl.DataAccessWindowSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

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
}
