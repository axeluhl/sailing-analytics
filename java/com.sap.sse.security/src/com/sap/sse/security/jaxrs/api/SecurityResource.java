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

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {
    /**
     * Can be used to figure out the current subject. Accepts the GET method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @GET
    @Path("/hello")
    @Produces("text/plain;charset=UTF-8")
    public Response sayHello() {
        return doSayHello();
    }

    private Response doSayHello() {
        final String messageText;
        final Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            messageText = "Hello "+subject.getPrincipal();
        } else {
            messageText = "Hello!";
        }
        return Response.ok(messageText, MediaType.TEXT_PLAIN_TYPE).build();
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