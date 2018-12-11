package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

@Path("/v1/usergroups")
public class UserGroupsResource extends AbstractSailingServerResource {
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getUserGroupsCurrentUserIsPartOf()
            throws ParseException, JsonDeserializationException {
        Response response = null;
        User user = getService().getSecurityService().getCurrentUser();
        if (user != null) {
            JSONObject root = new JSONObject();
            JSONArray groups = new JSONArray();
            root.put("groupsUserIsPartOf", groups);
            for (UserGroup group : user.getUserGroups()) {
                JSONObject groupJson = new JSONObject();
                groupJson.put("uuid", group.getId().toString());
                groupJson.put("name", group.getName());
                groups.add(groupJson);
            }
            response = Response.ok(root.toJSONString()).build();
        } else {
            response = Response.status(401).build();
        }
        return response;
    }
}