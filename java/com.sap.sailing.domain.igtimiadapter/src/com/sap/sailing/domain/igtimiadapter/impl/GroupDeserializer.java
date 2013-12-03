package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Group;

public class GroupDeserializer extends HasPermissionsDeserializer {
    public Group createGroupFromJson(JSONObject groupJson) {
        Boolean blob = (Boolean) groupJson.get("blob");
        return new GroupImpl((Long) groupJson.get("id"),
                (String) groupJson.get("name"),
                getPermissions((JSONObject) groupJson.get("permissions")),
                (Boolean) groupJson.get("hidden"),
                blob == null ? false : blob);
    }

}
