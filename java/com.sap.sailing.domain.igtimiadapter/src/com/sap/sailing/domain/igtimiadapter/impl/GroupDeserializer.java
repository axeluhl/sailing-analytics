package com.sap.sailing.domain.igtimiadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Group;

public class GroupDeserializer extends HasPermissionsDeserializer {
    public Group createGroupFromJson(JSONObject groupJson) {
        Boolean blob = (Boolean) groupJson.get("blob");
        final Object idObject = groupJson.get("id");
        final Long id;
        if (idObject instanceof String) { // this is a bug in a version of the Igtimi API of 2013-12-03
            id = Long.valueOf((String) idObject);
        } else {
            id = (Long) idObject;
        }
        return new GroupImpl(id,
                (String) groupJson.get("name"),
                getPermissions((JSONObject) groupJson.get("permissions")),
                (Boolean) groupJson.get("hidden"),
                blob == null ? false : blob);
    }

}
