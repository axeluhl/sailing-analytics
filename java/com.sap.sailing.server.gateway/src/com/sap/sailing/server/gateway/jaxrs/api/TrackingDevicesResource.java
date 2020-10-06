package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Set;
import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

@Path("/v1/tracking_devices")
public class TrackingDevicesResource extends AbstractSailingServerResource {

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("{deviceUUID}")
    public Response getDeviceStatus(@PathParam("deviceUUID") UUID deviceUUID) {
        final JsonSerializer<TrackingDeviceStatus> serializer = new TrackingDeviceStatusSerializer(
                new DeviceIdentifierJsonSerializer(
                        getServiceFinderFactory().createServiceFinder(DeviceIdentifierJsonHandler.class)));
        final JSONObject result = serializer.serialize(calculateDeviceStatus(deviceUUID));
        return Response.ok(streamingOutput(result)).build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    public Response getDeviceStatuses(@QueryParam("deviceUUIDs") Set<UUID> deviceUUIDs) {
        final JsonSerializer<TrackingDeviceStatus> serializer = new TrackingDeviceStatusSerializer(
                new DeviceIdentifierJsonSerializer(
                        getServiceFinderFactory().createServiceFinder(DeviceIdentifierJsonHandler.class)));
        final JSONArray result = new JSONArray();
        for (UUID deviceUUID : deviceUUIDs) {
            result.add(serializer.serialize(calculateDeviceStatus(deviceUUID)));
        }
        return Response.ok(streamingOutput(result)).build();
    }

    private TrackingDeviceStatus calculateDeviceStatus(UUID deviceUUID) {
        final DeviceIdentifier deviceIdentifier = new SmartphoneUUIDIdentifierImpl(deviceUUID);
        return TrackingDeviceStatus.calculateDeviceStatus(deviceIdentifier, getService());
    }
}