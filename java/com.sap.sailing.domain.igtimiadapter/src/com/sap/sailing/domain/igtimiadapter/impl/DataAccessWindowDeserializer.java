package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.SecurityEntity;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DataAccessWindowDeserializer extends HasPermissionsDeserializer {
    private static final Logger logger = Logger.getLogger(DataAccessWindowDeserializer.class.getName());
    
    public DataAccessWindow createDataAccessWindowFromJson(JSONObject resourceJson, IgtimiConnection conn) {
        return new DataAccessWindowImpl((Long) resourceJson.get("id"),
                new MillisecondsTimePoint(((Double) resourceJson.get("start_time")).longValue()),
                new MillisecondsTimePoint(((Double) resourceJson.get("end_time")).longValue()),
                (String) resourceJson.get("device_serial_number"),
                getPermissions((JSONObject) resourceJson.get("permissions")),
                getSecurityEntity((JSONObject) resourceJson.get("recipient")));
    }

    private SecurityEntity getSecurityEntity(JSONObject securityEntity) {
        if (securityEntity.containsKey("user")) {
            return new UserDeserializer().createUserFromJson((JSONObject) securityEntity.get("user"));
        } else if (securityEntity.containsKey("group")) {
            return new GroupDeserializer().createGroupFromJson((JSONObject) securityEntity.get("group"));
        } else {
            logger.warning("Don't know how to deserialize a serucity entity from "+securityEntity);
            return null;
        }
    }
}
