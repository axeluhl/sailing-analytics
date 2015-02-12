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

import org.json.simple.JSONObject;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.UserManagementException;

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {
    private static final Logger logger = Logger.getLogger(SecurityResource.class.getName());
    
    private Response getSecurityErrorResponse(String msg) {
        return  Response.status(Status.BAD_REQUEST).entity(msg).type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/hello")
    @Produces("application/json;charset=UTF-8")
    public Response sayHello() {
        return Response.ok("Hello!", MediaType.TEXT_PLAIN).build();
    }
    
    @POST
    @Path("/login")
    @Produces("application/json;charset=UTF-8")
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            getService().login(username, password);
            logger.info("Successfully logged in " + username + " with password");
        } catch (UserManagementException e) {
            logger.info("Logging in " + username + " with password failed: "+e.getMessage());
            return getSecurityErrorResponse(e.getMessage());
        }
        return Response.ok("Logged in!", MediaType.TEXT_PLAIN).build();
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
            response.put("access_token", getService().getAccessToken());
        } catch (UserManagementException e) {
            logger.info("Logging in " + username + " with password failed: "+e.getMessage());
            return getSecurityErrorResponse(e.getMessage());
        }
        return Response.ok("Logged in!", MediaType.TEXT_PLAIN).build();
    }
    
    @GET
    @Path("/logout")
    public Response logout() {
        getService().logout();
        return Response.ok("Logged out", MediaType.TEXT_PLAIN).build();
    }
}
 