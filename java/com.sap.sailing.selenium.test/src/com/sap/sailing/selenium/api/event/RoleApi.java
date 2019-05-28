package com.sap.sailing.selenium.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;
import com.sap.sse.common.Util;

public class RoleApi {

    private static final String ROLE_URL = "/api/restsecurity/role/";
    private static final String KEY_ROLE_NAME = "roleName";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_ID = "roleId";

    public Role getRole(ApiContext ctx, UUID roleId) {
        return new Role(ctx.get(ROLE_URL + roleId.toString()));
    }

    public Role createRole(ApiContext ctx, String roleName) {
        final Map<String, String> formParams = new HashMap<>();
        formParams.put(KEY_ROLE_NAME, roleName);
        return new Role(ctx.post(ROLE_URL, new HashMap<>(), formParams));
    }

    public void deleteRole(ApiContext ctx, UUID roleId) {
        ctx.delete(ROLE_URL + roleId.toString());
    }

    public String updateRole(ApiContext ctx, UUID roleId, Iterable<String> permissionStrings, String roleName) {
        final JSONObject json = new JSONObject();
        json.put(KEY_ROLE_NAME, roleName);
        final JSONArray permissionArray = new JSONArray();
        for (String permission : permissionStrings) {
            permissionArray.add(permission);
        }
        json.put(KEY_PERMISSIONS, permissionArray);
        return ctx.put(ROLE_URL + roleId.toString(), new HashMap<>(), json);
    }

    public static class Role extends JsonWrapper {

        public Role(JSONObject json) {
            super(json);
        }

        public String getName() {
            return get(KEY_ROLE_NAME);
        }

        public UUID getId() {
            return UUID.fromString(get(KEY_ID));
        }

        public Iterable<String> getPermissions() {
            JSONArray array = get(KEY_PERMISSIONS);
            Collection<String> col = new ArrayList<>();
            for (Object w : array) {
                col.add(w.toString());
            }
            return col;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            final String name = getName();
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            final Set<String> permissions = new HashSet<>();
            Util.addAll(getPermissions(), permissions);
            result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
            final UUID roleId = getId();
            result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Role other = (Role) obj;
            final String name = getName();
            final String otherName = other.getName();
            if (name == null) {
                if (otherName != null)
                    return false;
            } else if (!name.equals(otherName))
                return false;
            final Set<String> permissions = new HashSet<>();
            Util.addAll(getPermissions(), permissions);
            final Set<String> otherPermissions = new HashSet<>();
            Util.addAll(other.getPermissions(), otherPermissions);
            if (!permissions.equals(otherPermissions))
                return false;
            final UUID roleId = getId();
            final UUID otherRoleId = other.getId();
            if (roleId == null) {
                if (otherRoleId != null)
                    return false;
            } else if (!roleId.equals(otherRoleId))
                return false;
            return true;
        }

    }
}
