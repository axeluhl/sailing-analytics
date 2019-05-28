package com.sap.sailing.selenium.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RoleApi {

    private static final String ROLE_URL = "/api/restsecurity/role/";

    public Role getRole(ApiContext ctx, UUID roleId) {
        return new Role(ctx.get(ROLE_URL + roleId.toString()));
    }

    public Role createRole(ApiContext ctx, String roleName) {
        final Map<String, String> formParams = new HashMap<>();
        formParams.put("roleName", roleName);
        return new Role(ctx.post(ROLE_URL, new HashMap<>(), formParams));
    }

    public void deleteRole(ApiContext ctx, UUID roleId) {
        ctx.delete(ROLE_URL + roleId.toString());
    }

    public String updateRole(ApiContext ctx, UUID roleId, Iterable<String> permissionStrings, String roleName) {
        final JSONObject json = new JSONObject();
        json.put("roleName", roleName);
        final JSONArray permissionArray = new JSONArray();
        for (String permission : permissionStrings) {
            permissionArray.add(permission);
        }
        json.put("permissions", permissionArray);
        return ctx.put(ROLE_URL + roleId.toString(), new HashMap<>(), json);
    }

    public class Role extends JsonWrapper {

        public Role(JSONObject json) {
            super(json);
        }

        public String getName() {
            return get("name");
        }

        public UUID getId() {
            return UUID.fromString(get("id"));
        }

        public Iterable<String> getPermissions() {
            JSONArray array = get("permissions");
            Collection<String> col = new ArrayList<>();
            for (Object w : array) {
                col.add(w.toString());
            }
            return col;
        }
    }
}
