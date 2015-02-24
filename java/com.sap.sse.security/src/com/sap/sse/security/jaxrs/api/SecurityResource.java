package com.sap.sse.security.jaxrs.api;

import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONObject;

import com.sap.sse.security.User;
import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.UserManagementException;

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {
    private static final Logger logger = Logger.getLogger(SecurityResource.class.getName());
    
    private Response getSecurityErrorResponse(String msg) {
        return  Response.status(Status.BAD_REQUEST).entity(msg).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @GET
    @Path("/hello")
    @Produces("text/plain;charset=UTF-8")
    public Response sayHello() {
        final String messageText;
        if (SecurityUtils.getSubject().isAuthenticated()) {
            messageText = "Hello "+SecurityUtils.getSubject().getPrincipal();
        } else {
            messageText = "Hello!";
        }
        return Response.ok(messageText, MediaType.TEXT_PLAIN_TYPE).build();
    }
    
    @POST
    @Path("/login")
    @Produces("text/plain;charset=UTF-8")
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            getService().login(username, password);
            logger.info("Successfully logged in " + username + " with password");
        } catch (UserManagementException e) {
            logger.info("Logging in " + username + " with password failed: "+e.getMessage());
            return getSecurityErrorResponse(e.getMessage());
        }
        return Response.ok("Logged in!", MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("/access_token")
    @Produces("application/json;charset=UTF-8")
    public Response accessToken(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            getService().login(username, password);
            logger.info("Successfully logged in " + username + " with password");
            JSONObject response = new JSONObject();
            response.put("username", username);
            response.put("access_token", getService().createAccessToken(username));
            return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserManagementException e) {
            logger.info("Logging in " + username + " with password failed: "+e.getMessage());
            return getSecurityErrorResponse(e.getMessage());
        }
    }

    @POST
    @Path("/authenticate")
    @Produces("application/json;charset=UTF-8")
    public Response authenticate(@FormParam("access_token") String accessToken) {
        User user = getService().loginByAccessToken(accessToken);
        if (user == null) {
            logger.info("Invalid access token " + accessToken);
            return Response.status(Status.UNAUTHORIZED).entity("Access token " + accessToken + " not recognized")
                    .build();
        } else {
            logger.info("Authenticated " + user.getName() + " with access token");
        }
        JSONObject response = new JSONObject();
        response.put("username", user.getName());
        response.put("email", user.getEmail());
        return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @GET
    @Path("/logout")
    @Produces("text/plain;charset=UTF-8")
    public Response logout() {
        getService().logout();
        return Response.ok("Logged out", MediaType.TEXT_PLAIN_TYPE).build();
    }
}
 