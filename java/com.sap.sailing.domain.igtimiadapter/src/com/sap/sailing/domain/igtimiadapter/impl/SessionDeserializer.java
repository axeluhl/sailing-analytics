package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.Session;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SessionDeserializer extends HasPermissionsDeserializer {
    public Session createResourceFromJson(JSONObject sessionJson, IgtimiConnection conn) {
        Boolean blob = (Boolean) sessionJson.get("blob");
        final Double startTime = (Double) sessionJson.get("start_time");
        final Double endTime = (Double) sessionJson.get("end_time");
        return new SessionImpl((Long) sessionJson.get("id"), (String) sessionJson.get("name"), (Long) sessionJson.get("owner_id"),
                (Long) sessionJson.get("session_group_id"), (Long) sessionJson.get("admin_session_group_id"),
                getPermissions((JSONObject) sessionJson.get("permissions")),
                startTime==null?null:new MillisecondsTimePoint(startTime.longValue()),
                endTime==null?null:new MillisecondsTimePoint(endTime.longValue()),
                blob == null ? false : blob, conn);
    }
}
