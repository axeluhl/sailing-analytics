package com.sap.sailing.selenium.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;
import com.sap.sailing.selenium.api.event.RoleApi.Role;

public class UserGroupApi {

    private static final String KEY_GROUP_NAME = "groupName";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USERS = "users";
    private static final String KEY_ROLES = "roles";
    private static final String KEY_FOR_ALL = "forAll";
    private static final String KEY_TENANT_GROUP_UUID = "tenantGroup";

    private static final String USERGROUP_URL = "/api/restsecurity/usergroup/";
    private static final String USERGROUPS_URL = "/api/v1/usergroups/";
    private static final String SET_DEFAULT_TENANT_FOR_CURRENT_USER = USERGROUPS_URL + "setDefaultTenantForCurrentServerAndUser";
    private static final String ADD_USER_TO_USERGROUP_URL = USERGROUPS_URL + "addAnyUserToGroup";

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

    public Iterable<UserGroup> getReadableGroupsOfUser(ApiContext ctx, String userName) {
        JSONObject result = ctx.get(USERGROUPS_URL + "readable/" + userName);
        System.out.println(result.toJSONString());
        return ((JSONArray) result.get("readableGroups")).stream().map(UserGroup::new).collect(Collectors.toList());
    }

    public void addRoleToGroup(ApiContext ctx, UUID groupId, UUID roleId, boolean forAll) {
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put(KEY_FOR_ALL, Boolean.toString(forAll));
        ctx.put(USERGROUP_URL + groupId.toString() + "/role/" + roleId.toString(), new HashMap<>(), jsonBody);
    }

    public void removeRoleFromGroup(ApiContext ctx, UUID groupId, UUID roleId) {
        ctx.delete(USERGROUP_URL + groupId.toString() + "/role/" + roleId.toString());
    }

    public void setDefaultTenantForCurrentServerAndUser(ApiContext ctx, UUID tenantUuid) {
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(KEY_TENANT_GROUP_UUID, tenantUuid.toString());
        ctx.post(SET_DEFAULT_TENANT_FOR_CURRENT_USER, queryParams);
    }

    public void addUserToUserGroupWithoutPermissionOnUser(ApiContext ctx, String userName, UUID userGroupId) {
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(KEY_USER_NAME, userName);
        queryParams.put(KEY_GROUP_ID, userGroupId != null ? userGroupId.toString() : null);
        ctx.post(ADD_USER_TO_USERGROUP_URL, queryParams);
    }

    public class UserGroup extends JsonWrapper {

        public UserGroup(JSONObject json) {
            super(json);
        }

        public UserGroup(Object object) {
            super((JSONObject) object);
        }

        public UUID getGroupId() {
            if (getJson().containsKey("uuid")) {
                return UUID.fromString(get("uuid"));
            }
            return UUID.fromString(get(KEY_GROUP_ID));
        }

        public String getGroupName() {
            return get(KEY_GROUP_NAME);
        }

        public Iterable<String> getUsers() {
            JSONArray array = get(KEY_USERS);
            Collection<String> col = new ArrayList<>();
            if (array != null) {
                for (Object user : array) {
                    col.add(user.toString());
                }
            }
            return col;
        }

        public Iterable<Role> getRoles() {
            JSONArray array = get(KEY_ROLES);
            Collection<Role> col = new ArrayList<>();
            if (array != null) {
                for (Object roleJson : array) {
                    col.add(new Role((JSONObject) roleJson));
                }
            }
            return col;
        }
    }
}
