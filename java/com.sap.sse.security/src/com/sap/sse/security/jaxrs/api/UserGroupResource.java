package com.sap.sse.security.jaxrs.api;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.Util;
import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.RoleDefinition;
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
    public Response getUserGroupById(@QueryParam("id") String userGroupId,
            @QueryParam("groupname") String userGroupName) {

        Response response;
        if (userGroupId != null) {
            try {
                final UUID groupId = UUID.fromString(userGroupId);
                final UserGroup usergroup = getService().getUserGroup(groupId);
                response = handleExistingUserGroup(usergroup);
            } catch (IllegalArgumentException e) {
                response = Response.status(Status.BAD_REQUEST).entity("Invalid group id.").build();
            }
        }
        else if (userGroupName != null) {
            final UserGroup usergroup = getService().getUserGroupByName(userGroupName);
            response = handleExistingUserGroup(usergroup);
        }
        else {
            response = Response.status(Status.BAD_REQUEST).entity("Please specify either groupname or id.").build();
        }

        return response;
    }

    private Response handleExistingUserGroup(final UserGroup usergroup) {
        Response response;
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

    /** Returns a json object with id, groupname, roles and user names (filtered to thouse the current user can see). */
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

        final JSONArray jsonRolesOfGroup = new JSONArray();
        for (final Map.Entry<RoleDefinition, Boolean> roleDefinition : usergroup.getRoleDefinitionMap().entrySet()) {
            // filter users
            if (getService().hasCurrentUserReadPermission(roleDefinition.getKey())) {
                JSONObject groupJson = new JSONObject();
                groupJson.put("role_id", roleDefinition.getKey().getId().toString());
                groupJson.put("role_name", roleDefinition.getKey().getName());
                groupJson.put("associated", roleDefinition.getValue());
                jsonRolesOfGroup.add(groupJson);
            }
        }
        jsonResult.put("roles", jsonRolesOfGroup);
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

    @DELETE
    @Produces("application/json;charset=UTF-8")
    public Response deleteUserGroup(@QueryParam("groupName") String groupName) {
        Response response;
        final UserGroup usergroup = getService().getUserGroupByName(groupName);
        if (usergroup == null) {
            response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this name does not exist.").build();
        } else {
            if (getService().hasCurrentUserDeletePermission(usergroup)) {
                try {
                    getService().deleteUserGroup(usergroup);
                    response = Response.status(Status.NO_CONTENT).build();
                } catch (UserGroupManagementException e) {
                    response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
                }
            } else {
                response = Response.status(Status.UNAUTHORIZED).build();
            }
        }

        return response;
    }

    @Path("/user")
    @PUT
    @Produces("application/json;charset=UTF-8")
    public Response addUserToUserGroup(@QueryParam("groupName") String groupName,
            @QueryParam("username") String username) {
        final Response response;
        final UserGroup usergroup = getService().getUserGroupByName(groupName);
        if (usergroup == null) {
            response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this name does not exist.").build();
        } else {
            final User user = getService().getUserByName(username);
            if (user == null) {
                response = Response.status(Status.BAD_REQUEST).entity("User with this name does not exist.").build();
            } else {
                if (getService().hasCurrentUserReadPermission(usergroup)) {
                    if (Util.contains(usergroup.getUsers(), user)) {
                        response = Response.status(Status.BAD_REQUEST).entity("User is already in this group.").build();
                    } else {
                        if (getService().hasCurrentUserUpdatePermission(usergroup)) {
                            getService().addUserToUserGroup(usergroup, user);
                            response = Response.ok().build();
                        } else {
                            response = Response.status(Status.UNAUTHORIZED).build();
                        }

                    }
                } else {
                    response = Response.status(Status.UNAUTHORIZED).build();
                }
            }
        }
        return response;
    }

    @Path("/user")
    @DELETE
    @Produces("application/json;charset=UTF-8")
    public Response deleteUserFromUserGroup(@QueryParam("groupName") String groupName,
            @QueryParam("username") String username) {
        final Response response;
        final UserGroup usergroup = getService().getUserGroupByName(groupName);
        if (usergroup == null) {
            response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this name does not exist.").build();
        } else {
            final User user = getService().getUserByName(username);
            if (user == null) {
                response = Response.status(Status.BAD_REQUEST).entity("User with this name does not exist.").build();
            } else {
                if (getService().hasCurrentUserReadPermission(usergroup)) {
                    if (!Util.contains(usergroup.getUsers(), user)) {
                        response = Response.status(Status.BAD_REQUEST).entity("User is not in this group.").build();
                    } else {
                        if (getService().hasCurrentUserUpdatePermission(usergroup)) {
                            getService().removeUserFromUserGroup(usergroup, user);
                            response = Response.ok().build();
                        } else {
                            response = Response.status(Status.UNAUTHORIZED).build();
                        }

                    }
                } else {
                    response = Response.status(Status.UNAUTHORIZED).build();
                }
            }
        }
        return response;
    }

    @Path("/role")
    @PUT
    @Produces("application/json;charset=UTF-8")
    public Response addRoleToUserGroup(@QueryParam("groupName") String groupName,
            @QueryParam("roleId") String roleIdString, @QueryParam("roleAssociated") Boolean roleAssociated) {
        Response response;
        final UserGroup usergroup = getService().getUserGroupByName(groupName);

        try {
            final UUID roleId = UUID.fromString(roleIdString);
            if (usergroup == null) {
                response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this name does not exist.")
                        .build();
            } else {
                final RoleDefinition role = getService().getRoleDefinition(roleId);
                if (role == null) {
                    response = Response.status(Status.BAD_REQUEST).entity("Role with this id does not exist.").build();
                } else {
                    if (getService().hasCurrentUserUpdatePermission(usergroup)
                            && getService().hasCurrentUserReadPermission(role)) {
                        final Boolean associated = usergroup.getRoleAssociation(role);
                        if (associated == null || associated != roleAssociated) {
                            getService().putRoleDefinitionToUserGroup(usergroup, role, roleAssociated);
                            response = Response.ok().build();
                        } else {
                            response = Response.status(Status.BAD_REQUEST)
                                    .entity("Role was already added to the group.").build();
                        }
                    } else {
                        response = Response.status(Status.UNAUTHORIZED).build();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            response = Response.status(Status.BAD_REQUEST).entity("Invalid role id.").build();
        }
        return response;
    }

    @Path("/role")
    @DELETE
    @Produces("application/json;charset=UTF-8")
    public Response deleteRoleFromUserGroup(@QueryParam("groupName") String groupName,
            @QueryParam("roleId") String roleIdString, @QueryParam("roleAssociated") Boolean roleAssociated) {
        Response response;
        final UserGroup usergroup = getService().getUserGroupByName(groupName);

        try {
            final UUID roleId = UUID.fromString(roleIdString);
            if (usergroup == null) {
                response = Response.status(Status.BAD_REQUEST).entity("Usergroup with this name does not exist.")
                        .build();
            } else {
                final RoleDefinition role = getService().getRoleDefinition(roleId);
                if (role == null) {
                    response = Response.status(Status.BAD_REQUEST).entity("Role with this id does not exist.").build();
                } else {
                    if (getService().hasCurrentUserUpdatePermission(usergroup)
                            && getService().hasCurrentUserReadPermission(role)) {
                        if (usergroup.getRoleAssociation(role) != null) {
                            getService().removeRoleDefintionFromUserGroup(usergroup, role);
                            response = Response.status(Status.NO_CONTENT).build();
                        } else {
                            response = Response.status(Status.BAD_REQUEST)
                                    .entity("Role was already added to the group.").build();
                        }
                    } else {
                        response = Response.status(Status.UNAUTHORIZED).build();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            response = Response.status(Status.BAD_REQUEST).entity("Invalid role id.").build();
        }
        return response;
    }
}
