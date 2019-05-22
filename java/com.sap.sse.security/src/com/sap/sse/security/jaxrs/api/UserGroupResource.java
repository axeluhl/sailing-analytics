package com.sap.sse.security.jaxrs.api;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
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
                final JSONObject jsonResult = new JSONObject();
                jsonResult.put("id", usergroup.getId());
                jsonResult.put("groupname", usergroup.getName());

                final JSONArray jsonUsersInGroup = new JSONArray();
                for (final User user : usergroup.getUsers()) {
                    // filter users
                    if (getService().hasCurrentUserReadPermission(user)) {
                        jsonUsersInGroup.add(user.getId());
                    }
                }
                jsonResult.put("users", jsonUsersInGroup);
                response = Response.ok(jsonResult.toJSONString()).build();
            } else {
                response = Response.status(Status.UNAUTHORIZED).build();
            }
        }

        return response;
    }
}
