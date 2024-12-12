package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.impl.DeviceSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.rest.StreamingOutputUtil;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotDevicesResource.DEVICES)
public class RiotDevicesResource extends StreamingOutputUtil {
    protected static final String DEVICES = "/devices";
    
    @GET
    @Produces("text/plain;charset=UTF-8")
    public Response getDevices() throws ClientProtocolException, IllegalStateException, IOException, ParseException {
        final RiotServer riot = Activator.getInstance().getRiotServer();
        final JSONObject result = new JSONObject();
        final JSONArray resourcesJson = new JSONArray();
        result.put("devices", resourcesJson);
        for (final Device device : riot.getDevices()) {
            if (SecurityUtils.getSubject().isPermitted(device.getIdentifier().getStringPermission(DefaultActions.READ))) {
                resourcesJson.add(new DeviceSerializer().createJsonFromDevice(device));
            }
        }
        return Response.ok(streamingOutput(result)).build();
    }
}
