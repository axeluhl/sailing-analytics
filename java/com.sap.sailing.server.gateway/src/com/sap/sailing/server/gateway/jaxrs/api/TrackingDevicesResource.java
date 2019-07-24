package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixJsonSerializer;
import com.sap.sse.common.Timed;

@Path("/v1/tracking_devices")
public class TrackingDevicesResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(TrackingDevicesResource.class.getName());
    private final JsonSerializer<DeviceStatus> serializer = new DeviceStatusSerializer();
    
    private static final class DeviceStatus {
        private final UUID deviceUUID;
        private final GPSFix lastFix;

        public DeviceStatus(UUID deviceUUID, GPSFix lastFix) {
            this.deviceUUID = deviceUUID;
            this.lastFix = lastFix;
        }
    }
    
    private static final class DeviceStatusSerializer implements JsonSerializer<DeviceStatus> {
        private final GPSFixJsonSerializer gpsFixSerializer = new GPSFixJsonSerializer();
        @Override
        public JSONObject serialize(DeviceStatus deviceStatus) {
            JSONObject result = new JSONObject();
            result.put("deviceUUID", deviceStatus.deviceUUID.toString());
            if (deviceStatus.lastFix != null) {
                result.put("lastFix", gpsFixSerializer.serialize(deviceStatus.lastFix));
            }
            return result;
        }
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{deviceUUID}")
    public Response getDeviceStatus(@PathParam("deviceUUID") UUID deviceUUID) {
        JSONObject result = serializer.serialize(calculateDeviceStatus(deviceUUID));
        return Response.ok(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getDeviceStatuses(@QueryParam("deviceUUIDs") Set<UUID> deviceUUIDs) {
        JSONArray result = new JSONArray();
        for (UUID deviceUUID : deviceUUIDs) {
            result.add(serializer.serialize(calculateDeviceStatus(deviceUUID)));
        }
        return Response.ok(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8")
                .build();
    }
    
    private DeviceStatus calculateDeviceStatus(UUID deviceUUID) {
        final DeviceIdentifier deviceIdentifier = new SmartphoneUUIDIdentifierImpl(deviceUUID);
        try {
            Map<DeviceIdentifier, Timed> lastFixMap = getService().getSensorFixStore().getLastFix(Collections.singletonList(deviceIdentifier));
            Timed lastFix = lastFixMap.get(deviceIdentifier);
            GPSFix lastGPSFix = lastFix instanceof GPSFix ? (GPSFix) lastFix : null;
            return new DeviceStatus(deviceUUID, lastGPSFix);
        } catch (Exception e) {
            final String errorMessage = "Error while loading device status for device " + deviceUUID;
            logger.log(Level.SEVERE, errorMessage, e);
            throw new WebApplicationException(
                    Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMessage).build());
        }
    }
}