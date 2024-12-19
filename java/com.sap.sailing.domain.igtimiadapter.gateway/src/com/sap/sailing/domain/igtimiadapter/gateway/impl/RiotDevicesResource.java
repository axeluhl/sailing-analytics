package com.sap.sailing.domain.igtimiadapter.gateway.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.impl.DeviceDeserializer;
import com.sap.sailing.domain.igtimiadapter.impl.DeviceSerializer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

@Path(RestApiApplication.API + RestApiApplication.V1 + RiotDevicesResource.DEVICES)
public class RiotDevicesResource extends AbstractRiotServerResource {
    protected static final String DEVICES = "/devices";
    
    @GET
    @Produces("application/json;charset=UTF-8")
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
    
    @POST
    @Consumes("application/json;charset=UTF-8")
    public Response putDevice(@Context HttpServletRequest request) throws IOException, ParseException {
        final InputStream inputStream = request.getInputStream();
        final JSONObject deviceJson = (JSONObject) new JSONParser().parse(new InputStreamReader(inputStream));
        final Device device = new DeviceDeserializer().createDeviceFromJson(deviceJson);
        SecurityUtils.getSubject().checkPermission(device.getIdentifier().getStringPermission(DefaultActions.CREATE));
        final RiotServer riot = getRiotService();
        riot.addDevice(device);
        return Response.ok().build();
    }
}
