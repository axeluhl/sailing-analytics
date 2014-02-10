package com.sap.sailing.server.gateway.serialization.racelog.tracking;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.TransformationHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;

public interface DeviceIdentifierJsonHandler extends TransformationHandler<DeviceIdentifier, JSONObject> {
}
