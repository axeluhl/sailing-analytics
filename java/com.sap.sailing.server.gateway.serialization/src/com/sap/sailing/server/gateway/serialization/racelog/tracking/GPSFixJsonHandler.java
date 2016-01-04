package com.sap.sailing.server.gateway.serialization.racelog.tracking;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.TransformationHandler;
import com.sap.sailing.domain.common.tracking.GPSFix;

public interface GPSFixJsonHandler extends TransformationHandler<GPSFix, JSONObject> {

}
