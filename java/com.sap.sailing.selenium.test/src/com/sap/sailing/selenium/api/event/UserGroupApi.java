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
import com.sap.sailing.selenium.api.event.RoleApi.Role;

public class UserGroupApi {

    private static final String KEY_GROUP_NAME = "groupName";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_USERS = "users";
    private static final String KEY_ROLES = "roles";

    private static final String USERGROUP_URL = "/api/restsecurity/usergroup/";

    public UserGroup getUserGroup(ApiContext ctx, UUID groupId) {
        return new UserGroup(ctx.get(USERGROUP_URL + groupId.toString()));
    }

    public UserGroup getUserGroupByName(ApiContext ctx, String groupName) {
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(KEY_GROUP_NAME, groupName);
        return new UserGroup(ctx.get(USERGROUP_URL, queryParams));
    }

    public UserGroup createUserGroup(ApiContext ctx, String groupName) {
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put(KEY_GROUP_NAME, groupName);
        return new UserGroup(ctx.put(USERGROUP_URL, new HashMap<>(), jsonBody));
    }

    public void deleteUserGroup(ApiContext ctx, UUID groupId) {
        ctx.delete(USERGROUP_URL + groupId.toString());
    }

    public void addUserToGroup(ApiContext ctx, UUID groupId, String userName) {
        ctx.put(USERGROUP_URL + groupId.toString() + "/user/" + userName, new HashMap<>(), new JSONObject());
    }

    public void removeUserFromGroup(ApiContext ctx, UUID groupId, String userName) {
        ctx.delete(USERGROUP_URL + groupId.toString() + "/user/" + userName);
    }

    public void addRoleToGroup(ApiContext ctx, UUID groupId, UUID roleId) {
        ctx.put(USERGROUP_URL + groupId.toString() + "/role/" + roleId.toString(), new HashMap<>(), new JSONObject());
    }

    public void removeRoleFromGroup(ApiContext ctx, UUID groupId, UUID roleId) {
        ctx.delete(USERGROUP_URL + groupId.toString() + "/role/" + roleId.toString());
    }

    public class UserGroup extends JsonWrapper {

        public UserGroup(JSONObject json) {
            super(json);
        }

        public UUID getGroupId() {
            return UUID.fromString(get(KEY_GROUP_ID));
        }

        public String getGroupName() {
            return get(KEY_GROUP_NAME);
        }

        public Iterable<String> getUsers() {
            JSONArray array = get(KEY_USERS);
            Collection<String> col = new ArrayList<>();
            for (Object user : array) {
                col.add(user.toString());
            }
            return col;
        }

        public Iterable<Role> getRoles() {
            JSONArray array = get(KEY_ROLES);
            Collection<Role> col = new ArrayList<>();
            for (Object roleJson : array) {
                col.add(new Role((JSONObject) roleJson));
            }
            return col;
        }
    }
}
