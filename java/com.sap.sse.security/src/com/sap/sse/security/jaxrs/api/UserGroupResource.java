package com.sap.sse.security.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/restsecurity/usergroup")
public class UserGroupResource extends AbstractSecurityResource {
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getUserGroupById(@QueryParam("id") String userGroupId) {

        final Response response;
        final UUID groupId = UUID.fromString(userGroupId);
        final UserGroup usergroup = getService().getUserGroup(groupId);
        if (usergroup == null) {
            response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this id does not exist.").build();
        } else {
            if (getService().hasCurrentUserReadPermission(usergroup)) {
                final JSONObject jsonResult = convertUserGroupToJson(usergroup);
                response = Response.ok(jsonResult.toJSONString()).build();
            } else {
                response = Response.status(Status.UNAUTHORIZED).build();
            }
        }

        return response;
    }

    private JSONObject convertUserGroupToJson(final UserGroup usergroup) {
        final JSONObject jsonResult = new JSONObject();
        jsonResult.put("id", usergroup.getId().toString());
        jsonResult.put("groupname", usergroup.getName());

        final JSONArray jsonUsersInGroup = new JSONArray();
        for (final User user : usergroup.getUsers()) {
            // filter users
            if (getService().hasCurrentUserReadPermission(user)) {
                jsonUsersInGroup.add(user.getId());
            }
        }
        jsonResult.put("users", jsonUsersInGroup);
        System.out.println(jsonResult.toJSONString());
        return jsonResult;
    }

    @PUT
    @Produces("application/json;charset=UTF-8")
    public Response createUserGroup(@QueryParam("groupName") String groupName) {
        final Response response;
        final UserGroup usergroup = getService().getUserGroupByName(groupName);
        if (usergroup != null) {
            response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this name already exists.").build();
        } else {
            UUID newTenantId = UUID.randomUUID();
            UserGroup group = getService().setOwnershipWithoutCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredSecurityTypes.USER_GROUP, UserGroupImpl.getTypeRelativeObjectIdentifier(newTenantId),
                    groupName, () -> {
                        UserGroup userGroup;
                        try {
                            userGroup = getService().createUserGroup(newTenantId, groupName);
                        } catch (UserGroupManagementException e) {
                            throw new UserGroupManagementException(e.getMessage());
                        }
                        return userGroup;
                    });
            if (group == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not create user group.").build();
            } else {
                response = Response.status(Status.CREATED).entity(convertUserGroupToJson(group).toJSONString()).build();
            }
        }

        return response;
    }
}
