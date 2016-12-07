package com.sap.sse.security.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {
    /**
     * Can be used to figure out the current subject. Accepts the GET method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @GET
    @Path("/hello")
    @Produces("application/json;charset=UTF-8")
    public Response sayHello() {
        return doSayHello();
    }

    private Response doSayHello() {
        final Subject subject = SecurityUtils.getSubject();
        final JSONObject result = new JSONObject();
        result.put("principal", subject.getPrincipal().toString());
        result.put("authenticated", subject.isAuthenticated());
        result.put("remembered", subject.isRemembered());
        return Response.ok(result.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Can be used to figure out the current subject. Accepts the POST method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @POST
    @Path("/hello")
    @Produces("text/plain;charset=UTF-8")
    public Response sayHelloPost() {
        return doSayHello();
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

    @GET
    @Path("/remove_access_token")
    @Produces("application/json;charset=UTF-8")
    public Response removeAccessToken() {
        return removeAccessTokenPost();
    }

    @POST
    @Path("/remove_access_token")
    @Produces("application/json;charset=UTF-8")
    public Response removeAccessTokenPost() {
        final Response result;
        final Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal != null) {
            final String username = principal.toString();
            result = respondToRemoveAccessTokenForUser(username);
        } else {
            result = Response.status(Status.UNAUTHORIZED).build();
        }
        return result;
    }

    Response respondToRemoveAccessTokenForUser(final String username) {
        final Response result;
        getService().removeAccessToken(username);
        result = Response.ok().build();
        return result;
    }

    private Response respondWithAccessTokenForAuthenticatedSubject() {
        final Response result;
        final Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal != null) {
            final String username = principal.toString();
            result = respondWithAccessTokenForUser(username);
        } else {
            result = Response.status(Status.UNAUTHORIZED).build();
        }
        return result;
    }

    Response respondWithAccessTokenForUser(final String username) {
        JSONObject response = new JSONObject();
        response.put("username", username);
        String accessToken = getService().getOrCreateAccessToken(username);
        if (accessToken == null) {
            accessToken = getService().createAccessToken(username);
        }
        response.put("access_token", accessToken);
        return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }
}