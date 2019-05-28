package com.sap.sse.security.jaxrs.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.WildcardPermission;

@Path("/restsecurity/role")
public class RoleResource extends AbstractSecurityResource {

    @POST
    @Produces("text/plain;charset=UTF-8")
    public Response updatePermissionsForRole(@Context UriInfo uriInfo, @FormParam("roleId") String roleId,
            @FormParam("permissions") List<String> permissionStrings) {

        Response resp;
        try {

            // parse UUID
            final UUID roleUUID = UUID.fromString(roleId);

            // get role definition from role id
            final RoleDefinition roleDefinition = getService().getRoleDefinition(roleUUID);

            // null check role definition
            if (roleDefinition == null) {
                resp = Response.status(Status.NOT_FOUND).entity(String.format("No role with id '%s' found.", roleUUID))
                        .build();
            } else {
                // check update permission on role
                getService().checkCurrentUserUpdatePermission(roleDefinition);

                // create permission objects
                final Set<WildcardPermission> permissions = new HashSet<>();
                for (final String permissionString : permissionStrings) {
                    permissions.add(new WildcardPermission(permissionString));
                }

                // check only those meta-permissions which changed
                final Set<WildcardPermission> addedPermissions = new HashSet<>(roleDefinition.getPermissions());
                addedPermissions.removeAll(permissions);
                final Set<WildcardPermission> removedPermissions = new HashSet<>(permissions);
                removedPermissions.removeAll(roleDefinition.getPermissions());

                if (!getService().hasUserAllWildcardPermissionsForAlreadyRealizedQualifications(roleDefinition,
                        addedPermissions)) {
                    resp = Response.status(Status.UNAUTHORIZED)
                            .entity("Not permitted to grant permissions for role " + roleDefinition.getName()).build();
                } else if (!getService().hasUserAllWildcardPermissionsForAlreadyRealizedQualifications(roleDefinition,
                        removedPermissions)) {
                    resp = Response.status(Status.UNAUTHORIZED)
                            .entity("Not permitted to revoke permissions for role " + roleDefinition.getName()).build();
                } else {
                    // update role definitino with new permissions
                    roleDefinition.setPermissions(permissions);
                    getService().updateRoleDefinition(roleDefinition);
                    resp = Response.ok().build();
                }
            }
        } catch (IllegalArgumentException e) {
            resp = Response.status(Status.BAD_REQUEST).entity("Invalid roleId.").build();
        } catch (UnauthorizedException e) {
            resp = Response.status(Status.UNAUTHORIZED).build();
        }
        return resp;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getRole(@Context UriInfo uriInfo, @QueryParam("roleId") String roleId) {

        Response resp;
        try {

            // parse UUID
            final UUID roleUUID = UUID.fromString(roleId);

            // get role definition from role id
            final RoleDefinition roleDefinition = getService().getRoleDefinition(roleUUID);

            // null check role definition
            if (roleDefinition == null) {
                resp = Response.status(Status.NOT_FOUND).entity(String.format("No role with id '%s' found.", roleUUID))
                        .build();
            } else {
                // check read permission on role
                getService().checkCurrentUserReadPermission(roleDefinition);

                // build json result with permissions and id
                final JSONObject jsonResult = new JSONObject();
                final JSONArray jsonPermissions = new JSONArray();
                for (final WildcardPermission permission : roleDefinition.getPermissions()) {
                    jsonPermissions.add(permission.toString());
                }
                jsonResult.put("permissions", jsonPermissions);
                jsonResult.put("id", roleId);
                jsonResult.put("name", roleDefinition.getName());
                resp = Response.ok(jsonResult.toJSONString()).build();
            }
        } catch (IllegalArgumentException e) {
            resp = Response.status(Status.BAD_REQUEST).entity("Invalid roleId.").build();
        } catch (UnauthorizedException e) {
            resp = Response.status(Status.UNAUTHORIZED).build();
        }
        return resp;
    }
}
