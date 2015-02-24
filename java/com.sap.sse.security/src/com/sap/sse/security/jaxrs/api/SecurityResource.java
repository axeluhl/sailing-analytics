package com.sap.sse.security.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONObject;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {
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
    @Path("/hello")
    @Produces("text/plain;charset=UTF-8")
    public Response sayHelloPost() {
        final String messageText;
        if (SecurityUtils.getSubject().isAuthenticated()) {
            messageText = "Hello "+SecurityUtils.getSubject().getPrincipal();
        } else {
            messageText = "Hello!";
        }
        return Response.ok(messageText, MediaType.TEXT_PLAIN_TYPE).build();
    }

    @GET
    @Path("/access_token")
    @Produces("application/json;charset=UTF-8")
    public Response accessToken() {
        return respondWithAccessTokenForAuthenticatedSubject();
    }

    @POST
    @Path("/access_token")
    @Produces("application/json;charset=UTF-8")
    public Response accessTokenPost() {
        return respondWithAccessTokenForAuthenticatedSubject();
    }

    private Response respondWithAccessTokenForAuthenticatedSubject() {
        final String username = SecurityUtils.getSubject().getPrincipal().toString();
        return respondWithAccessTokenForUser(username);
    }

    Response respondWithAccessTokenForUser(final String username) {
        JSONObject response = new JSONObject();
        response.put("username", username);
        response.put("access_token", getService().createAccessToken(username));
        return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }
}