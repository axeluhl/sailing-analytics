package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.igtimiadapter.Permission;

public abstract class HasPermissionsDeserializer {
    protected Iterable<Permission> getPermissions(JSONObject permissions) {
        final List<Permission> result;
        if (permissions == null) {
            result = null;
        } else {
            result = new ArrayList<>();
            for (Entry<Object, Object> e : permissions.entrySet()) {
                if ((Boolean) e.getValue()) {
                    result.add(Permission.valueOf((String) e.getKey()));
                }
            }
        }
        return result;
    }
}
