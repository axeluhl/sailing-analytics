package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.Session;

public class SessionDeserializer extends HasPermissionsDeserializer {
    public Session createResourceFromJson(JSONObject sessionJson) {
        Boolean blob = (Boolean) sessionJson.get("blob");
        return new SessionImpl((Long) sessionJson.get("id"), (String) sessionJson.get("name"), (Long) sessionJson.get("owner_id"),
                (Long) sessionJson.get("session_group_id"), (Long) sessionJson.get("admin_session_group_id"),
                getPermissions((JSONObject) sessionJson.get("permissions")),
                new MillisecondsTimePoint(((Double) sessionJson.get("start_time")).longValue()),
                new MillisecondsTimePoint(((Double) sessionJson.get("end_time")).longValue()),
                blob == null ? false : blob);
    }
}
