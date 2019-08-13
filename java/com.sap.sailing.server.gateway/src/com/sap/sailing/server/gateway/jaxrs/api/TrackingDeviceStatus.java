package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Timed;

public final class TrackingDeviceStatus {
    private static final Logger logger = Logger.getLogger(TrackingDeviceStatus.class.getName());
    private final DeviceIdentifier deviceId;
    private final GPSFix lastFix;

    public TrackingDeviceStatus(DeviceIdentifier deviceId, GPSFix lastFix) {
        this.deviceId = deviceId;
        this.lastFix = lastFix;
    }

    public DeviceIdentifier getDeviceId() {
        return deviceId;
    }

    public GPSFix getLastGPSFix() {
        return lastFix;
    }
    
    public static TrackingDeviceStatus calculateDeviceStatus(DeviceIdentifier deviceIdentifier, RacingEventService service) {
        try {
            Map<DeviceIdentifier, Timed> lastFixMap = service.getSensorFixStore().getLastFix(Collections.singletonList(deviceIdentifier));
            Timed lastFix = lastFixMap.get(deviceIdentifier);
            GPSFix lastGPSFix = lastFix instanceof GPSFix ? (GPSFix) lastFix : null;
            return new TrackingDeviceStatus(deviceIdentifier, lastGPSFix);
        } catch (Exception e) {
            final String errorMessage = "Error while loading device status for device " + deviceIdentifier;
            logger.log(Level.SEVERE, errorMessage, e);
            throw new WebApplicationException(
                    Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMessage).build());
        }
    }
}