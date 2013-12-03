package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.SecurityEntity;

public class DataAccessWindowDeserializer extends HasPermissionsDeserializer {
    public DataAccessWindow createDataAccessWindowFromJson(JSONObject resourceJson, IgtimiConnection conn) {
        return new DataAccessWindowImpl((Long) resourceJson.get("id"),
                new MillisecondsTimePoint(((Double) resourceJson.get("start_time")).longValue()),
                new MillisecondsTimePoint(((Double) resourceJson.get("end_time")).longValue()),
                (String) resourceJson.get("device_serial_number"),
                getPermissions((JSONObject) resourceJson.get("permissions")),
                getSecurityEntity((JSONObject) resourceJson.get("recipient")));
    }

    private SecurityEntity getSecurityEntity(JSONObject jsonArray) {
        
        // TODO Auto-generated method stub
        return null;
    }
}
